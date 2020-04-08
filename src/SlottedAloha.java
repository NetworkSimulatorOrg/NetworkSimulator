import java.util.List;

public class SlottedAloha extends Aloha implements Protocol{

    private int frameSize;

    public SlottedAloha(int frameSize){
        this.frameSize = frameSize;
    }

    // @TODO
    public ProtocolState sendMsg(Node node, Message msg){
        // If synchronized,
        return super.sendMsg(node, msg);
        // TODO: otherwise, exit and wait for next iteration?
    }

    // @TODO
    public ProtocolState recvMsg(Node node, Message msg){
        return super.recvMsg(node, msg);
    }
}