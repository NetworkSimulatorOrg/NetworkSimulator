import java.util.List;

public class ConnectNode extends Node {
    public ConnectNode(int id, List<Node> adjacent, double propagationRate, double distance) {
        super(id, adjacent, propagationRate, distance);
    }

    public void sendMsg(Message msg) {
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
    public void sendMsgThread() {
    }

    public void recvMsgThread() {
        while (true) {
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
