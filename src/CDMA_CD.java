public class CDMA_CD extends Aloha implements Protocol {

    @Override
    public ProtocolState sendMsg(Node node, Message msg) throws InterruptedException {

        node.startSendingTimestamp = System.currentTimeMillis();

        // Keep resending the message until there is no collision
        while (true){

            // Probe the medium to check if it's idle
            if(node.sleepList.getNumMessages() > 0){
                // Wait until the last message detected would clear
                Thread.sleep((int)(node.sleepList.getLastTime() - System.currentTimeMillis()));

                // Recheck if there is a new incoming message
                continue;
            }

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
        if(Network.logToConsole) {
            System.out.println("Node " + node.getId() + " receiving " + msg.getPayload());
        }
        // Check if this node is sending
        if (node instanceof ComputeNode && node.isSending()){
            // This message that has been received and the message being sent by this node are corrupt.
            msg.setCorrupt();
            // CDMA_CD immediately stops sending if there is a corruption
            msg.notifyImmediately();

            // Set this node's message to corrupt and stop sending it
            ((ComputeNode)node).setSendingCorrupt();
            ((ComputeNode)node).getCurrentMessage().notifyImmediately();
            
            return ProtocolState.Failure;
        }

        msg.received();

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
