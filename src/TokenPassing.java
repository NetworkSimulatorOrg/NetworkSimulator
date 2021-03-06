import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TokenPassing implements Protocol {
    private final List<Node> nodeList;
    private Lock lock;
    private int sequenceNumber;
    private boolean force;
    private Node nextSender;
    private volatile Integer[] sync;

    /* Token Passing structure
     * Use an array, one entry per node
     * sending node waits for token.
     * Sync thread waits while sending
     * sending node notifies next sender when done
     * receiver waits on sender
     * sender notifies receiver
     *  receiver recvThread notifies sendThread to send if data is available.
     *  Otherwise, pass token to next sender.
     */

    public TokenPassing(List<Node> nodeList) {
        this.lock = new ReentrantLock();
        this.nodeList = nodeList;
        this.sequenceNumber = 100;
        this.force = true;

        sync = new Integer[Network.nodeCount];
        for(int i = 0; i < nodeList.size(); i++) {
            sync[i] = 0;
        }
    }

    private Node nextComputeNode(Node node) {
        int index = nodeList.indexOf(node);
        do {
            index = (index+1)%nodeList.size();
        }
        while(nodeList.get(index) instanceof ConnectNode);
        return nodeList.get(index);
    }

    private void passToken(Node node) throws InterruptedException {
        node.startSendingTimestamp = System.currentTimeMillis();

        ComputeNode compute = (ComputeNode) node;
        nextSender = nextComputeNode(node);
        Protocol.sendMsgHelper(node, new Message(node.getId(), sequenceNumber++, "Next Sender:" + nextSender.getId(), compute.getLastSenderStructure()));
    }

    @Override
    public ProtocolState sendMsg(Node node, Message msg) throws InterruptedException {
        node.startSendingTimestamp = System.currentTimeMillis();

        int idNumber = node.getIdNumber();
        // Set data send flag
        // Wait for permission
        sync[idNumber] = 1;
        if(force) {
            force = false;
        } else {
            synchronized (sync[idNumber]) {
                sync[idNumber].wait();
            }
        }

        synchronized (lock) {
            Protocol.sendMsgHelper(node, msg);

            passToken(node);

            sync[idNumber] = 0;
        }

        return msg.isCorrupt() ? ProtocolState.Failure : ProtocolState.Success;
    }

    @Override
    public ProtocolState recvMsg(Node node, Message msg) throws InterruptedException {
        Protocol.recvMsgHelper(node, msg);

        // Check for corruption and collision
        if (msg.isCorrupt()) {
            // Aloha does not stop sending the outgoing message. Do not end the sending delay.
            return ProtocolState.Failure;
        } else if (msg.getPayload().equals("Next Sender:" + node.getId())) {
            int idNumber = node.getIdNumber();

            // See if has data to send
            if (sync[idNumber] == 1) {
                synchronized (sync[idNumber]) {
                    sync[idNumber].notify();
                }
            } else {
                synchronized (lock) {
                    passToken(nextSender);
                }
            }
        }
        return ProtocolState.Success;
    }

    @Override
    public ProtocolState run() {
        return ProtocolState.Success;
    }

    @Override
    public ProtocolState terminateThreads() {
        return ProtocolState.Success;
    }
}
