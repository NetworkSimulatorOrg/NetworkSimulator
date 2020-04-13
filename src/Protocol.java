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
