import java.util.List;

public interface Protocol {
    ProtocolState sendMsg(Node node, Message msg);
    ProtocolState recvMsg(Node node, Message msg);
    // int handleCollision(); This is done in the recvMsg and send msg.
}
