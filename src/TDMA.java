public class TDMA implements Protocol {
    private int frameSize;
    private int nodeCount;
    private Object[] sync;
    private Thread synchronizing;
    private volatile boolean synchronizingRunning;

    /* TDMA structure
     * Use an array, one entry per node
     *      TODO: only have entries for compute nodes?
     * sending node waits for permission.
     * Sync thread waits while sending
     * sending node notifies sync thread when done
     */

    public TDMA(int frameSize, int nodeCount) {
        this.frameSize = frameSize;
        this.nodeCount = nodeCount;
        sync = new Object[nodeCount];
        for(var obj : sync) {
            obj = new Object();
        }

        synchronizing = new Thread(this::synchronizeThread);
    }

    private void synchronizeThread() {
        // After waiting
        synchronizingRunning = true;
        int step = 0;
        while(synchronizingRunning) {
            sync[step].notify();
            try {
                sync[step].wait();
            } catch (InterruptedException e) {
                synchronizingRunning = false;
            }
            step = (step + 1) % nodeCount;
        }
    }

    @Override
    public ProtocolState sendMsg(Node node, Message msg) throws InterruptedException {
        sync[Integer.parseInt(node.getId())].wait();

        // Send message

        sync[Integer.parseInt(node.getId())].notify();
        return null;
    }

    @Override
    public ProtocolState recvMsg(Node node, Message msg) {
        //return super.recvMsg(node, msg);
        return null;
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
