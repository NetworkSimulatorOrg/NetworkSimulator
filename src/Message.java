public class Message {
    private Node sender;
    private char[] payload;

    public Node getSender() {
        return sender;
    }

    public void setSender(Node sender) {
        this.sender = sender;
    }

    public char[] getPayload() {
        return payload;
    }

    public void setPayload(char[] payload) {
        this.payload = payload;
    }
}
