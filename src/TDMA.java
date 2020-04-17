import java.util.List;

public class TDMA implements Protocol {
    private List<Node> nodeList;
    private volatile Integer[] sync;
    private volatile Integer[] threads;
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
        sync = new Integer[nodeList.size()];
        threads = new Integer[nodeList.size()];
        for(int i = 0; i < nodeList.size(); i++) {
            sync[i] = 0;
            threads[i] = 0;
            if(nodeList.get(i) instanceof ConnectNode) {
                sync[i] = -1;
            }
        }

        synchronizing = new Thread(this::synchronizeThread);
    }

    private void synchronizeThread() {
        // After waiting
        synchronizingRunning = true;
        int step = 0;
        while(synchronizingRunning) {
            try {
                if (threads[step] == 1) {
                    long then = System.currentTimeMillis();

                    synchronized (threads[step]) {
                        threads[step] = 0;
                        threads[step].notify();
                    }

                    Thread.yield();

                    synchronized (sync[step]) {
                        sync[step].wait();
                    }

                    long now = System.currentTimeMillis();
                    timeoutEST = (long) ((1 - alpha) * timeoutEST + alpha * (now - then));
                    timeoutDEV = (long) ((1 - beta) * timeoutDEV + beta * Math.abs(now - then - timeoutEST));

                } else if (sync[step] != -1) {
                    System.out.println("System sleeping for " + (timeoutDEV + timeoutEST) + " milliseconds");
                    Thread.sleep(timeoutDEV + timeoutEST);
                }
            } catch (/*Interrupted*/Exception e) {
                e.printStackTrace();
                synchronizingRunning = false;
            }
            step = (step + 1) % this.nodeList.size();
        }
    }

    @Override
    public ProtocolState sendMsg(Node node, Message msg) throws InterruptedException {
        int idNumber = node.getIdNumber();
        synchronized (threads[idNumber]) {
            threads[idNumber] = 1;
            threads[idNumber].wait();
        }

            // Send message
        Protocol.sendMsgHelper(node, msg);

        synchronized (sync[idNumber]) {
            sync[idNumber].notify();
        }

        return ProtocolState.Success;
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
