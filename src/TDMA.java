import java.util.List;

public class TDMA implements Protocol {
    private List<Node> nodeList;
    private int frameSize;
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

    public TDMA(int frameSize, List<Node> nodeList) {
        this.frameSize = frameSize;
        this.nodeList = nodeList;
        sync = new Integer[nodeList.size()];
        for(int i = 0; i < nodeList.size(); i++) {
            sync[i] = 0;
            if(nodeList.get(i) instanceof ConnectNode)
                sync[i] = -1;
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

                } else if (sync[step] != -1){
                    Thread.sleep(timeoutDEV * 4 + timeoutEST);
                }
            } catch (InterruptedException e) {
                synchronizingRunning = false;
            } finally {
                sync[step] = 0;
            }
            step = (step + 1) % this.nodeList.size();
        }
    }

    @Override
    public ProtocolState sendMsg(Node node, Message msg) throws InterruptedException {
        sync[node.getIdNumber()] = 1;
        sync[node.getIdNumber()].wait();

        // Send message
        Protocol.sendMsgHelper(node, msg);

        sync[node.getIdNumber()].notify();
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
