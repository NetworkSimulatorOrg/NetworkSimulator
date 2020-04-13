public class Message {
    private final String sender;
    private String lastSender;
    private final int sequenceNumber;
    private long timestamp;
    private final String payload;
    private boolean corrupt;

    public Message(String sender, int sequenceNumber, String payload) {
        this.sender = sender;
        this.lastSender = sender;
        this.sequenceNumber = sequenceNumber;
        this.payload = payload;
        this.corrupt = false;
    }

    public Message clone() {
        Message copy = new Message(sender, sequenceNumber, payload);
        copy.lastSender = lastSender;
        copy.timestamp = timestamp;
        copy.corrupt = corrupt;
        return copy;
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
                tab + "| Last Sender: " + lastSender + "\t\t\t\t|\n" +
                tab + "| Corrupt: " + corrupt + "\t\t\t\t|\n" +
                tab + "+-------------------------------+\n"
        );
        return builder.toString();
    }

    public String getSender() {
        return sender;
    }

    public String getLastSender() {
        return lastSender;
    }

    public void setLastSender(String sender) {
        this.lastSender = sender;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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

    public void uncorrupt(){
        this.corrupt = false;
    }
}
