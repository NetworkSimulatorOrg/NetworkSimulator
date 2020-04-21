import java.util.Random;

public class ComputeNode extends Node {
    private int msgLength;
    private Protocol protocol;
    private Message sendingMsg;
    private double msgReadyToSendProbability;
    private Random random;
    protected String[] lastSenderStructure;

    private int sequenceNumber;

    public ComputeNode(String id, int propagationRate, int distance, int msgLength, Protocol protocol, double msgReadyToSendProbability) {
        super(id, propagationRate, distance);
        this.msgLength = msgLength;
        this.protocol = protocol;
        this.sequenceNumber = 0;
        this.msgReadyToSendProbability = msgReadyToSendProbability;
        this.random = new Random();

        // Create necessary threads
        sendingThread = new Thread(this::startSendMsgThread);
        receivingThread = new Thread(this::recvMsgThread);
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    private void nextMsg() {
        // The message will just contain the node's unique character repeatedly
        StringBuilder payload = new StringBuilder();
        for(int i = 0; i < msgLength; i++) {
            //payload.append((char)(i + 'a'));
            payload.append(getId());
        }

        this.sendingMsg = new Message(getId(), this.sequenceNumber++, payload.toString(), lastSenderStructure);
    }

    private void startSendMsgThread(){
        try{
            Thread.sleep(200);
        } catch (Exception e) {

        }
        // Prepare the first message sent from the node
        nextMsg();
        sendMsgThread();
    }

    private void sendMsgThread() {
        var run = true;
        long then;
        while(run) {
            try {
                // Tell the protocol to send the message and check if it sent correctly
                if (protocol.sendMsg(this, sendingMsg) == ProtocolState.Success) {
                    nextMsg();
                }

                // Delay to represent the new message being sent at a random time
                // TODO: Randomize delay
                do {
                    // sleep 1/100th of a second between generating doubles.
                    Thread.sleep(1000);
                } while(this.random.nextDouble() < 1 - this.msgReadyToSendProbability);
            } catch (/*Interrupted*/Exception e) {
                if(!(e instanceof InterruptedException))
                    e.printStackTrace();
                run = false;
            }
        }
        if(Network.logToConsole) {
            System.out.println("Compute Node " + getId() + " terminating sendMsgThread");
        }
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
                if(!(e instanceof InterruptedException)) {
                    e.printStackTrace();
                }
                sendingRunning = false;
            }
        }
        if(Network.logToConsole) {
            System.out.println("Compute Node " + getId() + " terminating recvMsgThread");
        }
    }

    public void setSendingCorrupt(){
        if (sendingMsg != null){
            sendingMsg.setCorrupt();
        }
    }

    protected void setLastSenderStructureSize(int size){
        lastSenderStructure = new String[size];
    }

    public String[] getLastSenderStructure() {
        return lastSenderStructure;
    }
}
