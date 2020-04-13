import java.util.List;

public class SlottedAloha extends Aloha implements Protocol{

    private int frameSize;
    private Object sync;
    private Thread synchroning;

    public SlottedAloha(int frameSize){
        this.frameSize = frameSize;
        sync = new Object();

        synchroning = new Thread(this::synchronizeThread);
    }

    private void synchronizeThread() {
        // After waiting
        var run = true;
        while(run) {
            sync.notifyAll();

            try {
                Thread.sleep(frameSize * 100);
            } catch (InterruptedException e) {
                run = false;
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
        synchroning.start();
        return ProtocolState.Success;
    }

    @Override
    public ProtocolState terminateThreads() {
        if(synchroning != null) {
            synchroning.interrupt();
        }
        return ProtocolState.Success;
    }
}