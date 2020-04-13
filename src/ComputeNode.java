import java.util.Random;

public class ComputeNode extends Node {
    private double msgProbability;
    private int msgLength;
    private Protocol protocol;
    private Message sendingMsg;
    private Random rand;

    private int sequenceNumber;

    public ComputeNode(String id, double propagationRate, double distance, double msgProbability, int msgLength, Protocol protocol) {
        super(id, propagationRate, distance);
        this.msgProbability = msgProbability;
        this.msgLength = msgLength;
        this.protocol = protocol;
        this.rand = new Random();

        // Prepare the first message sent from the node
        nextMsg();

        // Create necessary threads
        sendingThread = new Thread(this::startSendMsgThread);
        sendingThread.start();
        receivingThread = new Thread(this::recvMsgThread);
        receivingThread.start();

        // @TODO Figure out how we want to do sequence numbers
        this.sequenceNumber = 0;
    }

    private void nextMsg() {
        // The message will just contain the node's unique character repeatedly
        StringBuilder payload = new StringBuilder();
        for(int i = 0; i < msgLength; i++) {
            //payload.append((char)(i + 'a'));
            payload.append(getId());
        }

        this.sendingMsg = new Message(id, this.sequenceNumber++, payload.toString());
    }

    private void startSendMsgThread(){
        try{
            Thread.sleep(200);
            sendMsgThread();
        } catch (Exception e) {

        }
    }

    private void sendMsgThread() {
        var run = true;
        while(run) {
            try {
                // Check if the node should send the next message.
                // TODO: this probability is different than the probability of sending for a protocol.
                // I don't think that this is needed because the protocol should take care of this
                //if (msgProbability >= rand.nextDouble()) {
                    // Tell the protocol to send the message and check if it sent correctly
                    if (protocol.sendMsg(this, sendingMsg) == ProtocolState.Success) {
                        nextMsg();
                    }
                //}

                // TODO: Delay some way so that doubles aren't repeatedly being generated until it is lower than the probability. This depends on Protocol
                // Delay is hard coded to 1000 milliseconds.
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                run = false;
            }
        }
        System.out.println("Node " + getId() + " terminating sendMsgThread");
    }

    private void recvMsgThread() {
        Message msg = null;
        var run = true;
        while(run) {
            try {
                // Check if a message is in the queue
                if ((msg = sleepList.sleep()) != null) {
                    System.out.println("Compute " + getId() + ": Receiving message\n" + sendingMsg.toString("\t"));
                    if (protocol.recvMsg(this, msg) == ProtocolState.Success) {
                        sendReport(ReportType.Successful, msg, msg.getSender(), getId());
                    } else {
                        // Set the incoming message as a collision in the report
                        sendReport(ReportType.Collision, msg, msg.getSender(), getId());

                        // Set the outgoing message as a collision in the report
                        sendReport(ReportType.Collision, sendingMsg, getId(), msg.getSender());
                    }
                }

                //Thread.sleep(delay);
            } catch (/*Interrupted*/Exception e) {
                run = false;
            }
        }
        System.out.println("Node " + getId() + " terminating recvMsgThread");
    }

    public void setSendingCorrupt(){
        if (sendingMsg != null){
            sendingMsg.setCorrupt();
        }
    }

}
