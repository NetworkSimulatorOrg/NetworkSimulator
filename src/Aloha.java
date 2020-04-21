
/* For Aloha, if a message collides with another,
 * the message should be resent at some later point in time.
 * This will be simulated by generating a random number 0 to the number of ComputeNodes.
 * The next message will be sent at this random number * propagation rate * the node's longest distance
 * after the sending delay ends.
 */

public class Aloha implements Protocol {
    @Override
    public ProtocolState sendMsg(Node node, Message msg) throws InterruptedException {
        node.startSendingTimestamp = System.currentTimeMillis();

        // Keep resending the message until there is no collision
        while (true){
            Protocol.sendMsgHelper(node, msg);

            // Check if this message collided
            if (msg.isCorrupt()) {
                // Resend the message at some future time.
                int delay = (int) (Math.random() * Network.computeNodeCount * node.longestDistance * node.propagationRate);
                if (Network.logToConsole) {
                    System.out.println("Node " + node.getId() + " will be delayed for " + delay + " ms.");
                }
                Thread.sleep(delay);
                msg.prepareForRetransmission();
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
        Protocol.recvMsgHelper(node, msg);

        // Check for corruption and collision
        if(msg.isCorrupt())
            // Aloha does not stop sending the outgoing message. Do not end the sending delay.
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
