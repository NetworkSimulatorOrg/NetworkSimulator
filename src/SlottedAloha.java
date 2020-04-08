import java.util.List;

public class SlottedAloha extends Aloha implements Protocol{

    private int frameSize;
    private Object sync;
    private Thread synchroning;

    public SlottedAloha(int frameSize){
        this.frameSize = frameSize;
        sync = new Object();

        synchroning = new Thread(this::synchronizeThread);
        synchroning.start();
    }

    private void synchronizeThread() {
        // After waiting
        while(true) {
            sync.notifyAll();

            try {
                Thread.sleep(frameSize * 100);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
    }

    // @TODO
    public ProtocolState sendMsg(Node node, Message msg){
        try {
            sync.wait();
            return super.sendMsg(node, msg);
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        return ProtocolState.Error;
    }

    // @TODO
    public ProtocolState recvMsg(Node node, Message msg){
        return super.recvMsg(node, msg);
    }
}