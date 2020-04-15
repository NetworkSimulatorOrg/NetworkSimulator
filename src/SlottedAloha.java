public class SlottedAloha extends Aloha implements Protocol{

    private int frameSize;
    private Object sync;
    private Thread synchronizing;
    private volatile boolean synchronizingRunning;

    public SlottedAloha(int frameSize){
        this.frameSize = frameSize;
        sync = new Object();

        synchronizing = new Thread(this::synchronizeThread);
    }

    private void synchronizeThread() {
        // After waiting
        synchronizingRunning = true;
        while(synchronizingRunning) {
            sync.notifyAll();

            try {
                Thread.sleep(frameSize * 100);
            } catch (InterruptedException e) {
                synchronizingRunning = false;
            }
        }
    }

    // @TODO
    public ProtocolState sendMsg(Node node, Message msg) throws InterruptedException {
        sync.wait();
        return super.sendMsg(node, msg);
    }

    // @TODO
    public ProtocolState recvMsg(Node node, Message msg){
        return super.recvMsg(node, msg);
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