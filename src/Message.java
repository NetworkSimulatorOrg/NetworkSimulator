public class Message {
    private final String sender;
    private String lastSender;
    private final int sequenceNumber;
    private long timestamp;
    private final String payload;
    private boolean corrupt;

    public Message(String sender, int sequenceNumber, long timestamp, String payload) {
        this.sender = sender;
        this.lastSender = sender;
        this.sequenceNumber = sequenceNumber;
        this.timestamp = timestamp;
        this.payload = payload;
        this.corrupt = false;
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
}
