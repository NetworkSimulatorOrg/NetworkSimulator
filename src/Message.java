public class Message {
    private final String sender;
    private String[] lastSenders;
    private final int sequenceNumber;
    private int repeatCount;
    //private long[] timestamps;
    private final String payload;
    private boolean corrupt;
    private int nodesRemaining;


    public Message(String sender, int sequenceNumber, String payload, String[] lastSenders) {
        this.sender = sender;
        this.sequenceNumber = sequenceNumber;
        this.payload = payload;
        this.corrupt = false;
        this.repeatCount = 0;
        this.lastSenders = lastSenders;
        //timestamps = new long[Network.nodeCount];
        nodesRemaining = Network.computeNodeCount - 1;
    }

    @Override
    public String toString() {
        return toString("", null);
    }

    public String toString(String tab, Node receivedAt) {
        StringBuilder builder = new StringBuilder();
        builder.append(
                tab + "+-------------------------------+\n" +
                tab + "| Originator: " + sender + "\t\t\t\t\t|\n" +
                tab + "| Sequence number: " + sequenceNumber + "\t\t\t|\n" +
                tab + "| Repeat Transmission Count: " + repeatCount + "\t|\n" +
                tab + "| Payload: " + payload + "\t|\n" +
                tab + "+ - - Current Hop - - - - - - - +\n");
        if(receivedAt != null) {
            builder.append(
                    tab + "| Received At: " + receivedAt.getId() + "\t\t\t\t|\n" +
                    tab + "| Last Sender: " + lastSenders[receivedAt.getIdNumber()] + "\t\t\t\t|\n"
            );
        }
        builder.append(
                tab + "| Corrupt: " + (corrupt ? ("true\t\t\t\t\t|\n") : ("false\t\t\t\t|\n")) +
                tab + "+-------------------------------+\n"
        );
        return builder.toString();
    }

    public String getSender() {
        return sender;
    }

    public String getLastSender(Node node) {
        return lastSenders[node.getIdNumber()];
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public String getPayload() {
        return payload;
    }

    public boolean isCorrupt() {
        return corrupt;
    }

    public void setCorrupt() {
        this.corrupt = true;
    }

    public synchronized void prepareForRetransmission() {
        repeatCount++;
        this.corrupt = false;
        // Reset the number of compute nodes that have to acknowledge this message
        nodesRemaining = Network.computeNodeCount - 1;
    }

    public synchronized void received() {
        // A compute node has acknowledged this message (Success or Collision)
        nodesRemaining--;

        // If all ComputeNodes have been reached, stop the sender's waiting
        if (nodesRemaining == 0) {
            this.notifyAll();
        }
    }

    public synchronized void notifyImmediately(){
        // Used for CSMA_CD to end transmission upon detection of a collision
        this.notifyAll();
    }
}
