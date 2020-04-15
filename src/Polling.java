public class Polling implements Protocol {
    private volatile Integer sync;
    private volatile Object wakeUp;
    private Thread synchronizing;
    private volatile boolean synchronizingRunning;

    /* Polling structure
     * sending node waits for permission.
     * Notify one at a time
     * Sync thread waits while sending
     * sending node notifies sync thread when done
     *
     * If no object notified, keep on trying.
     */

    public Polling() {
        sync = 0;
        wakeUp = new Object();

        synchronizing = new Thread(this::synchronizeThread);
    }

    private void synchronizeThread() {
        // After waiting
        synchronizingRunning = true;
        while(synchronizingRunning) {
            if (sync > 0) {
                try {
                    synchronized (sync) {
                        sync--;
                    }
                    sync.notify();
                    wakeUp.wait();
                } catch (InterruptedException e) {
                    synchronizingRunning = false;
                } finally {
                }
            }
        }
    }

    @Override
    public ProtocolState sendMsg(Node node, Message msg) throws InterruptedException {
        synchronized (sync) {
            sync++;
        }
        sync.wait();

        // Send message
        Protocol.sendMsgHelper(node, msg);

        wakeUp.notify();
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
