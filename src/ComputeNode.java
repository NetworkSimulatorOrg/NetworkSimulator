import java.util.List;
import java.util.Random;

public class ComputeNode extends Node {
    private double msgProbability;
    private int msgLength;
    private Protocol protocol;
    private Message msg;
    private Random rand;

    public ComputeNode(int id, List<Node> adjacent, double propagationRate, double distance, double msgProbability, int msgLength, Protocol protocol) {
        super(id, adjacent, propagationRate, distance);
        this.msgProbability = msgProbability;
        this.msgLength = msgLength;
        this.protocol = protocol;
        this.rand = new Random();
        nextMsg();
    }

    private void nextMsg() {
        StringBuilder payload = new StringBuilder();
        for(int i = 0; i < msgLength; i++) {
            payload.append(i + 'a');
        }
        this.msg = new Message(id, this.msg.getSequenceNumber(), System.currentTimeMillis(), payload.toString());
    }

    public void sendMsg(Message msg) {
        if(msgProbability < rand.nextDouble())
            return;

        protocol.sendMsg(msg);

        msg.setLastSender(id);
        for(Node node : adjacent) {
            super.sendMsg(msg, node.getId());
        }
    }

    public void recvMsg(Message msg) {
        // Mark node receiving
        boolean collision = addReceiver() > 1;

        super.recvMsg(msg);

        protocol.recvMsg(msg);

        propagationDelay();

        collision = collision || removeReceiver() > 0;

        // Report collision
    }


}
