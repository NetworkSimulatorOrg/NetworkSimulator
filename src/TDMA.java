public class TDMA implements Protocol {
    private int frameSize;
    private int nodeCount;
    private volatile Integer[] sync;
    private Thread synchronizing;
    private long timeoutEST;
    private long timeoutDEV;
    private double alpha = 0.125;
    private double beta = 0.25;
    private volatile boolean synchronizingRunning;

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

    public TDMA(int frameSize, int nodeCount) {
        this.frameSize = frameSize;
        this.nodeCount = nodeCount;
        sync = new Integer[nodeCount];
        for(int i = 0; i < nodeCount; i++) {
            sync[i] = 0;
        }

        synchronizing = new Thread(this::synchronizeThread);
    }

    private void synchronizeThread() {
        // After waiting
        synchronizingRunning = true;
        int step = 0;
        while(synchronizingRunning) {
            try {
                if (sync[step] == 1) {
                    long then = System.currentTimeMillis();

                    sync[step].notify();
                    Thread.yield();
                    sync[step].wait();

                    long now = System.currentTimeMillis();
                    timeoutEST = (long) ((1 - alpha) * timeoutEST + alpha * (now - then));
                    timeoutDEV = (long) ((1 - beta) * timeoutDEV + beta * Math.abs(now - then - timeoutEST));

                } else {
                    Thread.sleep(timeoutDEV * 4 + timeoutEST);
                }
            } catch (InterruptedException e) {
                synchronizingRunning = false;
            } finally {
                sync[step] = 0;
            }
            step = (step + 1) % nodeCount;
        }
    }

    @Override
    public ProtocolState sendMsg(Node node, Message msg) throws InterruptedException {
        sync[Integer.parseInt(node.getId())] = 1;
        sync[Integer.parseInt(node.getId())].wait();

        // Send message
        Protocol.sendMsgHelper(node, msg);

        sync[Integer.parseInt(node.getId())].notify();
        return null;
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
