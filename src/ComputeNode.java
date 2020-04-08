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

    private int sequenceNumber;

    public ComputeNode(int id, List<Node> adjacent, double propagationRate, double distance, double msgProbability, int msgLength, Protocol protocol) {
        super(id, adjacent, propagationRate, distance);
        this.msgProbability = msgProbability;
        this.msgLength = msgLength;
        this.protocol = protocol;
        this.rand = new Random();

        // Prepare the first message sent from the node
        nextMsg();

        // Create necessary threads
        sending = new Thread(this::sendMsgThread);
        sending.start();
        receiving = new Thread(this::recvMsgThread);
        receiving.start();

        // @TODO Figure out how we want to do sequence numbers
        sequenceNumber = 0;
    }

    private void nextMsg() {
        // The message will just contain the node's unique character repeatedly
        StringBuilder payload = new StringBuilder();
        for(int i = 0; i < msgLength; i++) {
            payload.append(i + 'a');
        }

        this.msg = new Message(id, this.sequenceNumber++, System.currentTimeMillis(), payload.toString());
    }

    private void sendMsgThread() {
        while(true) {
            // Check if the node should send the next message.
            // TODO: this probability is different than the probability of sending for a protocol.
            if (msgProbability >= rand.nextDouble()) {
                // Tell the protocol to send the message and check if it sent correctly
                if(protocol.sendMsg(this, msg) == ProtocolState.Success) {
                    nextMsg();
                }
            }

            // @TODO: Delay some way so that doubles aren't repeatedly being generated until it is lower than the probability.
            // Delay is hard coded to 1000 milliseconds.
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
    }

    private void recvMsgThread() {
        Message msg = null;
        while(true) {
            // Check if a message is in the queue
            if ((msg = sleepList.sleep()) != null) {
                if(protocol.recvMsg(this, msg) == ProtocolState.Success) {
                    sendReport(ReportType.Successful, msg, msg.getSender(), getId());
                } else {
                    sendReport(ReportType.Collision, msg, msg.getSender(), getId());
                }
            }

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
    }

}
