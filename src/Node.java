import java.util.ArrayList;
import java.util.List;

public class Node {
    protected final String id;
    private Boolean sending;
    private Integer receivingCount;
    protected final List<Node> adjacent;
    protected DeltaList sleepList;
    protected double propagationRate;
    protected double distance;
    protected double longestDistance;
    protected long delay = 1000;
    protected Thread receivingThread = null;
    protected Thread sendingThread = null;

    public Node(String id, double propagationRate, double distance) {
        this.id = id;
        this.sending = false;
        this.receivingCount = 0;
        this.adjacent = new ArrayList<>();
        this.sleepList = new DeltaList();
        this.propagationRate = propagationRate;
        this.distance = distance;
        this.longestDistance = distance;
    }

    public void terminateThreads() {
        if(receivingThread != null) {
            receivingThread.interrupt();
        }
        if(sendingThread != null) {
            sendingThread.interrupt();
        }
    }

    public int getReceivingCount() {
        return receivingCount;
    }

    public boolean isSending() {
        return sending;
    }

    public boolean setSending(boolean sending) {
        synchronized (this.sending) {
            this.sending = sending;
        }
        return sending;
    }

    public boolean isReceiving() {
        return receivingCount > 0;
    }

    public int addReceiver() {
        synchronized (receivingCount) {
            receivingCount++;
        }
        return receivingCount;
    }

    public int removeReceiver() {
        synchronized (receivingCount) {
            receivingCount--;
        }
        return receivingCount;
    }

    public String getId() {
        return id;
    }

    // This is only used for ComputeNode's
    public void sendingDelay() throws InterruptedException {
        // Sleep until the message would have reached every node
         Thread.sleep((long) (propagationRate * longestDistance * 1.1));
    }

    protected Message propagationDelay() {
        // Sleep the thread until the first message is ready to send
        return sleepList.sleep();
    }

    public Node getAdjacentNodeByID(String id) {
        for(int i = 0; i < adjacent.size(); i++) {
            if(adjacent.get(i).getId().equals(id)) {
                return adjacent.get(i);
            }
        }
        return null;
    }

    // Finds node, and passes the message to the node's synchronized queue.
    public void sendMsg(Message msg, String receiver) {
        Node recv = getAdjacentNodeByID(receiver);

        if(recv != null) {
            // pass message to recv
            System.out.println("Node " + getId() + " sending " + msg.getPayload() + " to node " + recv.getId());

            // Set the timestamp at which the message would be received
            msg.setTimestamp(System.currentTimeMillis());
            recv.sleepList.push((long) (propagationRate * distance) + msg.getTimestamp(), msg);
        }
    }

    // Currently unused
    public Message recvMsg() {
        // Mark node receiving
        boolean collision = addReceiver() > 1 || isSending();

        // Receiving thread waits to simulate propagation delay
        Message msg = propagationDelay();

        // Test for collision at end
        collision = collision || removeReceiver() > 0 || isSending();

        if(collision) {
            msg.setCorrupt();
            sendReport(ReportType.Collision, msg, msg.getSender(), id);
        } else {
            sendReport(ReportType.Successful, msg, msg.getSender(), id);
        }

        return msg;
    }

    public void sendReport(ReportType type, Message msg, String sender, String receiver) {
        Report report = new Report(type, sender, receiver, msg);
        // Send report to network.
        Network.network.sendReport(report);
    }



    public void addAdjacentNode(Node node){
        adjacent.add(node);
    }
}
