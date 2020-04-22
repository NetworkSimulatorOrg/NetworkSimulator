public class CDMA_CD extends Aloha implements Protocol {

    @Override
    public ProtocolState sendMsg(Node node, Message msg) throws InterruptedException {

        /*
         * Notes:
         * Before sending, probe for messages. Maybe check incoming queue?
         * While waiting for responses, at the first collision detection, stop waiting.
         */


        return null;
    }

    @Override
    public ProtocolState recvMsg(Node node, Message msg) {
        return null;
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
