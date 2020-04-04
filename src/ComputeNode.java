import java.util.List;
import java.util.Random;

public class ComputeNode extends Node {
    private double msgProbability;
    private int msgLength;
    private Protocol protocol;
    private Message msg;
    private Random rand;
    private Thread sending;
    private Thread receiving;

    public ComputeNode(int id, List<Node> adjacent, double propagationRate, double distance, double msgProbability, int msgLength, Protocol protocol) {
        super(id, adjacent, propagationRate, distance);
        this.msgProbability = msgProbability;
        this.msgLength = msgLength;
        this.protocol = protocol;
        this.rand = new Random();
        nextMsg();

        // Create necessary threads
        sending = new Thread(this::sendMsgThread);
        sending.start();
        receiving = new Thread(this::recvMsgThread);
        receiving.start();
    }

    private void nextMsg() {
        StringBuilder payload = new StringBuilder();
        for(int i = 0; i < msgLength; i++) {
            payload.append(i + 'a');
        }
        this.msg = new Message(id, this.msg.getSequenceNumber(), System.currentTimeMillis(), payload.toString());
    }

    private void sendMsgThread() {
        while(true) {
            if (msgProbability >= rand.nextDouble()) {
                if(protocol.sendMsg(msg, adjacent) == ProtocolState.Success) {
                    setSending(true);

                    try {
                        Thread.sleep((long) (propagationRate * distance));
                    } catch (InterruptedException e) {
                        System.out.println(e);
                    }

                    setSending(false);

                    nextMsg();
                }
            }

            //Thread.sleep(delay);
        }
    }

    private void recvMsgThread() {
        while(true) {
            if (messages.size() > 0) {
                Message msg = recvMsg();

                protocol.recvMsg(msg);
            }
            //Thread.sleep(delay);
        }
    }

}
