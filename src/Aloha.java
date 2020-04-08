public class Aloha implements Protocol {
    @Override
    public ProtocolState sendMsg(Node node, Message msg) {
        // Handle node state
        node.setSending(true);

        // Send to all nodes
        for(Node adjacent : node.adjacent) {
            node.sendMsg(msg, adjacent.getId());
        }

        // Sleep the sending thread so that it doesn't try to send another until the first one would be received.
        node.sendingDelay();

        // Handle node state
        node.setSending(false);

        // No check for state of line, so always return success.
        return ProtocolState.Success;
    }

    @Override
    public ProtocolState recvMsg(Node node, Message msg) {
        // Check for corruption and collision
        if(msg.isCorrupt())
            return ProtocolState.Failure;
        return ProtocolState.Success;
    }
}
