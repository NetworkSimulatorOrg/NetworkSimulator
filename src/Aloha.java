
/* For Aloha, if a message collides with another,
 * the message should be resent at some later point in time.
 * This will be simulated by generating a random number 0 to (2 * Number of nodes).
 * The next message will be sent at this random number * propagation rate * longest distance
 * after the sending delay ends.
 */

public class Aloha implements Protocol {
    @Override
    public ProtocolState sendMsg(Node node, Message msg) throws InterruptedException {

        // Keep resending the message until there is no collision
        while (true){
            // Handle node state
            node.setSending(true);

            StringBuilder builder = new StringBuilder();
            builder.append("Compute ");
            builder.append(node.getId());
            builder.append(": Generating message\n");
            builder.append(msg.toString("\t"));

            // Send to all nodes
            for(Node adjacent : node.adjacent) {
                node.sendMsg(msg, adjacent.getId());
                builder.append("\t To ");
                builder.append(adjacent.getId());
                builder.append("\n");
            }

            System.out.println(builder.toString());


            // Sleep the sending thread so that it doesn't try to send another until the first one would be received by every node.
            node.sendingDelay();

            // Handle node state
            node.setSending(false);

            // Check if this message collided
            if (msg.isCorrupt()){
                // Resend the message at some future time.
                int delay = (int) (Math.random() * Network.computeNodeCount * node.longestDistance * node.propagationRate);
                System.out.println("Node " + node.getId() + " will be delayed for " + delay + " ms.");
                Thread.sleep(delay);
                msg.uncorrupt();
                msg.setLastSender(node.getId());
            }
            else{
                break;
            }

        }

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
    public ProtocolState run() {
        return null;
    }

    @Override
    public ProtocolState terminateThreads() {
        return null;
    }
}
