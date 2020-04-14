public class Message {
    private final String sender;
    private String[] lastSenders;
    private final int sequenceNumber;
    //private long[] timestamps;
    private final String payload;
    private boolean corrupt;
    private int nodesRemaining;


    public Message(String sender, int sequenceNumber, String payload, String[] lastSenders) {
        this.sender = sender;
        this.sequenceNumber = sequenceNumber;
        this.payload = payload;
        this.corrupt = false;
        this.lastSenders = lastSenders;
        //timestamps = new long[Network.nodeCount];
        nodesRemaining = Network.computeNodeCount - 1;
    }

    @Override
    public String toString() {
        return toString("");
    }

    public String toString(String tab) {
        StringBuilder builder = new StringBuilder();
        builder.append(
                tab + "+-------------------------------+\n" +
                tab + "| Originator: " + sender + "\t\t\t\t\t|\n" +
                tab + "| Sequence number: " + sequenceNumber + "\t\t\t|\n" +
                tab + "| Payload: " + payload + "\t|\n" +
                tab + "+ - - Current Hop - - - - - - - +\n" +
                // TODO: Log last sender
                //tab + "| Last Sender: " + lastSender + "\t\t\t\t|\n" +
                tab + "| Corrupt: " + corrupt + "\t\t\t\t|\n" +
                tab + "+-------------------------------+\n"
        );
        return builder.toString();
    }

    public String getSender() {
        return sender;
    }

    public String getLastSender(String nodeId) {
        return lastSenders[Integer.parseInt(nodeId)];
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    /*
    public long getTimestamp(String nodeId) {
        return timestamps[Integer.parseInt(nodeId)];
    }

    public void setTimestamp(long timestamp, String nodeId) {
        timestamps[Integer.parseInt(nodeId)] = timestamp;
    }
    */

    public String getPayload() {
        return payload;
    }

    public boolean isCorrupt() {
        return corrupt;
    }

    public void setCorrupt() {
        this.corrupt = true;
    }

    public void uncorrupt(){
        this.corrupt = false;
    }

    public void resetNodesRemaining(){
        // Reset the number of compute nodes that have to acknowledge this message
        synchronized(this){
            nodesRemaining = Network.computeNodeCount - 1;
        }
    }

    public void received(){
        // A compute node has acknowledged this message (Success or Collision)
        synchronized(this){
            nodesRemaining--;

            // If all ComputeNodes have been reached, stop the sender's waiting
            if (nodesRemaining == 0){
                this.notifyAll();
            }
        }
    }
}
