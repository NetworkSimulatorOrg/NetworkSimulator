import java.util.List;

public class SlottedAloha implements Protocol{

    private int frameSize;

    public SlottedAloha(int frameSize){
        this.frameSize = frameSize;
    }
    
    // @TODO
    public ProtocolState sendMsg(Message msg, List<Node> id){
        for(Node node : id){
            node.sendMsg(msg, node.id);
        }

        return ProtocolState.Success;
    }

    // @TODO
    public ProtocolState recvMsg(Message msg){
        return ProtocolState.Fail;
    }
}