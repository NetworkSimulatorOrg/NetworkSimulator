import java.util.List;

public class ConnectNode extends Node {
    private Thread receiving;

    public ConnectNode(int id, List<Node> adjacent, double propagationRate, double distance) {
        super(id, adjacent, propagationRate, distance);

        // Create necessary threads
        receiving = new Thread(this::recvMsgThread);
        receiving.start();
    }

    private void sendMsg(Message msg) {
        // Send the message to all adjacent nodes besides the one that sent it,
        int lastSender = msg.getLastSender();
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
        while (true) {
            // Check if a message is in the queue
            if (messages.size() > 0) {
                Message msg = recvMsg();
                // Send out message. Time delay has passed.

                if (msg != null) {
                    sendMsg(msg);
                }
            }
            //Thread.sleep(delay);
        }
    }
}
