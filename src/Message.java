public class Message {
    private final int sender;
    private final int sequenceNumber;
    private long timestamp;
    private int lastSender;
    private final String payload;
    private boolean corrupt;

    public Message(int sender, int sequenceNumber, long timestamp, String payload) {
        this.sender = sender;
        this.sequenceNumber = sequenceNumber;
        this.timestamp = timestamp;
        this.payload = payload;
        this.corrupt = false;
    }

    public int getSender() {
        return sender;
    }

    public int getLastSender() {
        return lastSender;
    }

    public void setLastSender(int sender) {
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
