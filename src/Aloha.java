public class Aloha implements Protocol {
    @Override
    public ProtocolState sendMsg(Node node, Message msg) {
        node.setSending(true);

        for(Node adjacent : node.adjacent) {
            node.sendMsg(msg, adjacent.getId());
        }

        // Sleep the sending thread so that it doesn't try to send another until the first one would be received.
        node.sendingDelay();

        node.setSending(false);

        return ProtocolState.Success;
    }

    @Override
    public ProtocolState recvMsg(Node node, Message msg) {
        if(msg.isCorrupt() || (node.isSending() && node.isReceiving()))
            return ProtocolState.Failure;
        return ProtocolState.Success;
    }
}
