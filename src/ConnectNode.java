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

    public void recvMsg(Message msg, int sender) {
        // Mark node receiving
        boolean collision = addReceiver() > 1;

        super.recvMsg(msg, sender);

        propagationDelay();

        collision = collision || removeReceiver() > 0;

        // Report collision
    }

}
