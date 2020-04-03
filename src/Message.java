public class Message {
    private final int sender;
    private int lastSender;
    private final char[] payload;

    public Message(int sender, char[] payload) {
        this.sender = sender;
        this.payload = payload;
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

    public char[] getPayload() {
        return payload;
    }
}
