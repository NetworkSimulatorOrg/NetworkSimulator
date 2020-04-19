import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TDMA implements Protocol {
    private List<Node> nodeList;
    private Message[] messages;
    private Thread synchronizing;
    private volatile boolean synchronizingRunning;
    private long timeoutEST = 0;
    private long timeoutDEV = 0;
    private final double alpha = 0.125;
    private final double beta = 0.25;

    /* TDMA structure
     * Use an array, one entry per node
     *      TODO: only have entries for compute nodes?
     * sending node waits for permission.
     * Sync thread waits while sending
     * sending node notifies sync thread when done
     * If no object notified, delay the thread with the exponential weighted average.
     *      Taken from notes on TCP RTT & Timeout
     *      This allows us not to configure the network to where we must provide a number to ensure the package is sent.
     */

    public TDMA(List<Node> nodeList) {
        this.nodeList = nodeList;
        messages = new Message[nodeList.size()];
        for (int i = 0; i < nodeList.size(); i++) {
            int index = nodeList.get(i).getIdNumber();
            messages[index] = null;
        }

        synchronizing = new Thread(this::synchronizeThread);
    }

    private void synchronizeThread() {
        // After waiting
        synchronizingRunning = true;
        for(int step = 0; synchronizingRunning; step = (step + 1) % this.nodeList.size()) {
            try {
                if (nodeList.get(step) instanceof ConnectNode) continue;
                if(messages[step] == null) {
                    System.out.println("System sleeping for " + (timeoutDEV + timeoutEST) + " milliseconds");
                    Thread.sleep(timeoutDEV + timeoutEST);
                } else {
                    long then = System.currentTimeMillis();

                    // Resynchronize
                    Protocol.sendMsgHelper(Network.network.getNodeById(messages[step].getSender()), messages[step]);

                    assert messages[step] != null;
                    synchronized (messages[step]) {
                        messages[step].notify();
                        messages[step] = null;
                    }

                    long now = System.currentTimeMillis();
                    timeoutEST = (long) ((1 - alpha) * timeoutEST + alpha * (now - then));
                    timeoutDEV = (long) ((1 - beta) * timeoutDEV + beta * Math.abs(now - then - timeoutEST));
                }
            } catch (/*Interrupted*/Exception e) {
                e.printStackTrace();
                synchronizingRunning = false;
            }
        }
    }

    @Override
    public ProtocolState sendMsg(Node node, Message msg) throws InterruptedException {
        int idNumber = node.getIdNumber();
        messages[idNumber] = msg;

        synchronized (messages[idNumber]) {
            messages[idNumber].wait();
        }

        return msg.isCorrupt() ? ProtocolState.Failure : ProtocolState.Success;
    }

    @Override
    public ProtocolState recvMsg(Node node, Message msg) {
        Protocol.recvMsgHelper(node, msg);

        // Check for corruption and collision
        if(msg.isCorrupt())
            // Aloha does not stop sending the outgoing message. Do not end the sending delay.
            return ProtocolState.Failure;
        return ProtocolState.Success;
    }

    @Override
    public ProtocolState run() {
        synchronizing.start();
        return ProtocolState.Success;
    }

    @Override
    public ProtocolState terminateThreads() {
        if(synchronizing != null) {
            synchronizing.interrupt();
        }
        synchronizingRunning = false;
        return ProtocolState.Success;
    }
}
