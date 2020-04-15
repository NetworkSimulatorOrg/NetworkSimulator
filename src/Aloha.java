
/* For Aloha, if a message collides with another,
 * the message should be resent at some later point in time.
 * This will be simulated by generating a random number 0 to the number of ComputeNodes.
 * The next message will be sent at this random number * propagation rate * the node's longest distance
 * after the sending delay ends.
 */

public class Aloha implements Protocol {
    @Override
    public ProtocolState sendMsg(Node node, Message msg) throws InterruptedException {

        // Keep resending the message until there is no collision
        while (true){
            Protocol.sendMsgHelper(node, msg);

            // Check if this message collided
            if (msg.isCorrupt()){
                // Report this as a collision
                Protocol.sendReport(ReportType.Collision, msg, node.getId());
                
                // Resend the message at some future time.
                int delay = (int) (Math.random() * Network.computeNodeCount * node.longestDistance * node.propagationRate);
                System.out.println("Node " + node.getId() + " will be delayed for " + delay + " ms.");
                Thread.sleep(delay);
                msg.prepareForRetransmission();
            }
            else{
                // Send a report that the message was successfully received by all nodes.
                Protocol.sendReport(ReportType.Successful, msg, node.getId());
                break;
            }

        }

        // No check for state of line, so always return success.
        return ProtocolState.Success;
    }

    @Override
    public ProtocolState recvMsg(Node node, Message msg) {
        System.out.println("Node " + node.getId() + " receiving " + msg.getPayload());
        // Check if this node is sending
        if (node instanceof ComputeNode && node.isSending()){
            // This message that has been received and the message being sent by this node are corrupt.
            msg.setCorrupt();
            ((ComputeNode)node).setSendingCorrupt();

            // Aloha does not stop sending the outgoing message. Do not end the sending delay.
        }

        msg.received();

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
