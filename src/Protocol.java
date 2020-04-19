public interface Protocol {
    /*
     * Takes in the sending node, msg to send
     * Handles node state sending changes.
     *   MUST CALL NODE.SETSENDING() AND SENDINGDELAY();
     * Handles collisions if protocol specifies.
     * Returns when done sending
     */
    ProtocolState sendMsg(Node node, Message msg) throws InterruptedException;

    /*
     * Performs grunt work of sending out the message. Same for all nodes.
     */
    static void sendMsgHelper(Node node, Message msg) throws InterruptedException {
        // Handle node state
        node.setSending(true);

        StringBuilder builder = new StringBuilder();
        builder.append("Compute ");
        builder.append(node.getId());
        builder.append(": Generating message\n");
        builder.append(msg.toString("\t", node.getId()));

        // Send to all nodes
        for (Node adjacent : node.adjacent) {
            node.sendMsg(msg, adjacent.getId());
            builder.append("\t To ");
            builder.append(adjacent.getId());
            builder.append("\n");
        }

        System.out.println(builder.toString());


        // Wait the sending thread so that it doesn't try to send another until the first one has been received by every node.
        node.sendingDelay(msg);

        // Handle node state
        node.setSending(false);

        // Check if this message collided
        if (msg.isCorrupt()) {
            // Report this as a collision
            Protocol.sendReport(ReportType.Collision, msg, node.getId());
        } else {
            // Send a report that the message was successfully received by all nodes.
            Protocol.sendReport(ReportType.Successful, msg, node.getId());
        }

    }


    static void sendReport(ReportType type, Message msg, String sender) {
        Report report = new Report(type, sender, msg);
        // Send report to network.
        Network.network.sendReport(report);
    }


    /*
     * Takes in sending node, the message received
     * The propagation delay has already occurred to receive the message,
     *   so no need to check for the node's current state.
     * Returns SUCCESS if successful reception.
     * Returns FAILURE if failed reception.
     * Returns ERROR if an error occured.
     */
    ProtocolState recvMsg(Node node, Message msg) throws InterruptedException;

    static void recvMsgHelper(Node node, Message msg) {
        System.out.println("Node " + node.getId() + " receiving " + msg.getPayload());
        // Check if this node is sending
        if (node instanceof ComputeNode && node.isSending()){
            // This message that has been received and the message being sent by this node are corrupt.
            msg.setCorrupt();
            ((ComputeNode)node).setSendingCorrupt();
        }

        msg.received();
    }


    ProtocolState run();
    ProtocolState terminateThreads();
}
