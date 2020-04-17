import java.util.List;

public class TokenPassing implements Protocol {
    private final List<Node> nodeList;
    private int sequenceNumber;
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
        this.nodeList = nodeList;
        this.sequenceNumber = 100;

        sync = new Integer[Network.nodeCount];
        for(int i = 0; i < nodeList.size(); i++) {
            sync[i] = 0;
            if(nodeList.get(i) instanceof ConnectNode)
                sync[i] = -1;
        }
    }

    private int nextComputeNode(int index) {
        do {
            index = (index+1)%nodeList.size();
        }
        while(sync[index] == -1);
        return index;
    }

    private void passToken(Node node) throws InterruptedException {
        ComputeNode compute = (ComputeNode) node;
        nextSender = nodeList.get(nextComputeNode(node.getIdNumber()));
        Protocol.sendMsgHelper(node, new Message(node.getId(), sequenceNumber++, "Next Sender:" + nextSender.getId(), compute.getLastSenderStructure()));
    }

    @Override
    public ProtocolState sendMsg(Node node, Message msg) throws InterruptedException {
        // Set data send flag
        sync[node.getIdNumber()] = 1;
        // Wait for permission
        sync[node.getIdNumber()].wait();

        Protocol.sendMsgHelper(node, msg);

        passToken(node);

        sync[node.getIdNumber()].notify();

        return null;
    }

    @Override
    public ProtocolState recvMsg(Node node, Message msg) throws InterruptedException {
        Protocol.recvMsgHelper(node, msg);

        // Check for corruption and collision
        if (msg.isCorrupt()) {
            // Aloha does not stop sending the outgoing message. Do not end the sending delay.
            return ProtocolState.Failure;
        } else if (msg.getPayload().equals("Next Sender:" + node.getId())) {
            // Wait until sender is done sending
            sync[Network.network.getNodeById(msg.getSender()).getIdNumber()].wait();
            int idNumber = node.getIdNumber();

            // See if has data to send
            if (sync[idNumber] == 1) {
                sync[idNumber].notify();
            } else {
                passToken(nextSender);
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
