public class Aloha implements Protocol {
    @Override
    public ProtocolState sendMsg(Node node, Message msg) throws InterruptedException {
        // Handle node state
        node.setSending(true);

        // Send to all nodes
        for(Node adjacent : node.adjacent) {
            node.sendMsg(msg, adjacent.getId());
        }

        // Sleep the sending thread so that it doesn't try to send another until the first one would be received by every node.
        node.sendingDelay();

        // Handle node state
        node.setSending(false);

        // No check for state of line, so always return success.
        return ProtocolState.Success;
    }

    @Override
    public ProtocolState recvMsg(Node node, Message msg) {
        // Check if this node is sending
        if (node instanceof ComputeNode && node.isSending()){
            // This message that has been received and the message being sent by this node are corrupt.
            msg.setCorrupt();
            ((ComputeNode)node).setSendingCorrupt();

            // Aloha does not stop sending the outgoing message. Do not end the sending delay.
        }

        // Check for corruption and collision
        if(msg.isCorrupt())
            return ProtocolState.Failure;
        return ProtocolState.Success;
    }

    @Override
    public ProtocolState terminateThreads() {
        return null;
    }
}
