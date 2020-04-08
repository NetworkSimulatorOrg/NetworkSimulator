public class Aloha implements Protocol {
    @Override
    public ProtocolState sendMsg(Node node, Message msg) {
        for(Node adjacent : node.adjacent) {
            node.sendMsg(msg, adjacent.getId());
        }
        return ProtocolState.Success;
    }

    @Override
    public ProtocolState recvMsg(Node node, Message msg) {
        if(msg.isCorrupt())
            return ProtocolState.Failure;
        return ProtocolState.Success;
    }
}
