public class ConnectNode extends Node {

    public ConnectNode(String id, double propagationRate, double distance) {
        super(id, propagationRate, distance);

        // Create necessary threads
        receivingThread = new Thread(this::recvMsgThread);
        receivingThread.start();
    }

    private void sendMsg(Message msg) {
        // Send the message to all adjacent nodes besides the one that sent it,
        var lastSender = msg.getLastSender();
        msg.setLastSender(id);
        for(Node node : adjacent) {
            if (node.getId() != lastSender) {
                super.sendMsg(msg, node.getId());
            }
        }
    }

    // This is not needed. Messages actually being passed between
    // threads is done in the thread of the sending node. Only 1
    // thread is needed for a connect node.
    private void sendMsgThread() {
    }

    private void recvMsgThread() {
        Message msg = null;
        var run = true;
        while(run) {
            // Check if a message is in the queue
            // Send out message. Time delay has passed.
            try {
                if ((msg = sleepList.sleep()) != null) {
                    sendMsg(msg);
                }

                Thread.sleep(delay);
            } catch(InterruptedException e) {
                run = false;
            }
        }
    }
}
