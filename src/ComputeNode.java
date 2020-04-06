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

        // Immediately start sending a message
        nextMsg();

        // Create necessary threads
        sending = new Thread(this::sendMsgThread);
        sending.start();
        receiving = new Thread(this::recvMsgThread);
        receiving.start();
    }

    private void nextMsg() {
        // The message will just contain the node's unique character repeatedly
        StringBuilder payload = new StringBuilder();
        for(int i = 0; i < msgLength; i++) {
            payload.append(i + 'a');
        }
        this.msg = new Message(id, this.msg.getSequenceNumber(), System.currentTimeMillis(), payload.toString());
    }

    private void sendMsgThread() {
        while(true) {
            // Check if the node should send the next message
            if (msgProbability >= rand.nextDouble()) {
                // Tell the protocol to send the message and check if it sent correctly
                if(protocol.sendMsg(msg, adjacent) == ProtocolState.Success) {
                    setSending(true);

                    // Sleep the sending thread so that it doesn't try to send another until the first one would be received.
                    try {
                        Thread.sleep((long) (propagationRate * distance));
                    } catch (InterruptedException e) {
                        System.out.println(e);
                    }

                    setSending(false);

                    nextMsg();
                }
            }

            // @TODO: Delay some way so that doubles aren't repeatedly being generated.
            //Thread.sleep(delay);
        }
    }

    private void recvMsgThread() {
        Message msg = null;
        while(true) {
            // Check if a message is in the queue
            if ((msg = sleepList.sleep()) != null) {
                protocol.recvMsg(msg);
            }
            //Thread.sleep(delay);
        }
    }

}
