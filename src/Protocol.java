import java.util.List;

public interface Protocol {
    ProtocolState sendMsg(Message msg, List<Node> id);
    ProtocolState recvMsg(Message msg);
    // int handleCollision(); This is done in the recvMsg and send msg.
}
