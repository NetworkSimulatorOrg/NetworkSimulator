public class TokenPassing implements Protocol {
    private volatile Object[] waiting;

    /* Token Passing structure
     * sending node waits for permission from previous node in order.
     * Notify next thread when done, one at a time
     * Sending thread is visited regardless if has message to send
     */

    public TokenPassing() {
        waiting = new Object[Network.nodeCount];
    }

    @Override
    public ProtocolState sendMsg(Node node, Message msg) throws InterruptedException {
        waiting[Integer.parseInt(node.getId())].wait();

        // TODO: if message to send, send a message.
        // Send message
        Protocol.sendMsgHelper(node, msg);

        waiting[Integer.parseInt(node.getId()) % Network.nodeCount].notify();
        return null;
    }

    @Override
    public ProtocolState recvMsg(Node node, Message msg) {
        //return super.recvMsg(node, msg);
        return null;
    }

    @Override
    public ProtocolState run() {
        return ProtocolState.Success;
    }

    @Override
    public ProtocolState terminateThreads() {
        return ProtocolState.Success;
    }
}
