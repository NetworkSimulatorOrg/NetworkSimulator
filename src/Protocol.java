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
    }


    /*
     * Takes in sending node, the message received
     * The propagation delay has already occurred to receive the message,
     *   so no need to check for the node's current state.
     * Returns SUCCESS if successful reception.
     * Returns FAILURE if failed reception.
     * Returns ERROR if an error occured.
     */
    ProtocolState recvMsg(Node node, Message msg);

    ProtocolState run();
    ProtocolState terminateThreads();
}
