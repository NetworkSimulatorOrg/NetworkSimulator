import java.util.Random;

public class ComputeNode extends Node {
    private double msgProbability;
    private int msgLength;
    private Protocol protocol;
    private Message sendingMsg;
    private Random rand;
    protected String[] lastSenderStructure;

    private int sequenceNumber;

    public ComputeNode(String id, double propagationRate, double distance, double msgProbability, int msgLength, Protocol protocol) {
        super(id, propagationRate, distance);
        this.msgProbability = msgProbability;
        this.msgLength = msgLength;
        this.protocol = protocol;
        this.rand = new Random();

        // Create necessary threads
        sendingThread = new Thread(this::startSendMsgThread);
        receivingThread = new Thread(this::recvMsgThread);

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

        this.sendingMsg = new Message(id, this.sequenceNumber++, payload.toString(), lastSenderStructure);
    }

    private void startSendMsgThread(){
        try{
            Thread.sleep(200);
            // Prepare the first message sent from the node
            nextMsg();
            sendMsgThread();
        } catch (Exception e) {

        }
    }

    private void sendMsgThread() {
        var run = true;
        while(run) {
            try {
                
                // Tell the protocol to send the message and check if it sent correctly
                if (protocol.sendMsg(this, sendingMsg) == ProtocolState.Success) {
                    nextMsg();
                }

                // Delay to represent the new message being sent at a random time
                // TODO: Randomize delay
                Thread.sleep(delay);
            } catch (/*Interrupted*/Exception e) {
                System.out.println(e.toString());
                run = false;
            }
        }
        System.out.println("Node " + getId() + " terminating sendMsgThread");
    }

    private void recvMsgThread() {
        Message msg = null;
        sendingRunning = true;
        while(sendingRunning) {
            try {
                // Check if a message is in the queue
                if ((msg = sleepList.sleep()) != null) {
                    protocol.recvMsg(this, msg);
                }

            } catch (Exception e) {
                System.out.println(e.toString());
                e.printStackTrace(System.out);
                sendingRunning = false;
            }
        }
        System.out.println("Node " + getId() + " terminating recvMsgThread");
    }

    public void setSendingCorrupt(){
        if (sendingMsg != null){
            sendingMsg.setCorrupt();
        }
    }

    protected void setLastSenderStructureSize(int size){
        lastSenderStructure = new String[size];
    }

}
