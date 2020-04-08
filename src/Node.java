import java.util.List;
import java.util.concurrent.SynchronousQueue;

public class Node {
    protected final int id;
    private Boolean sending;
    private Integer receivingCount;
    protected final List<Node> adjacent;
    protected DeltaList sleepList;
    protected double propagationRate;
    protected double distance;

    public Node(int id, List<Node> adjacent, double propagationRate, double distance) {
        this.id = id;
        this.sending = false;
        this.receivingCount = 0;
        this.adjacent = adjacent;
        this.sleepList = new DeltaList();
        this.propagationRate = propagationRate;
        this.distance = distance;
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

    public int getId() {
        return id;
    }

    public void sendingDelay() {
        try {
            Thread.sleep((long) (propagationRate * distance));
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    }

    protected Message propagationDelay() {
        // Sleep the thread until the first message is ready to send
        return sleepList.sleep();
    }

    public Node getAdjacentNodeByID(int id) {
        for(int i = 0; i < adjacent.size(); i++) {
            if(adjacent.get(i).getId() == id) {
                return adjacent.get(i);
            }
        }
        return null;
    }

    // Finds node, and passes the message to the node's synchronized queue.
    public void sendMsg(Message msg, int receiver) {
        Node recv = getAdjacentNodeByID(receiver);

        if(recv != null) {
            // pass message to recv
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

    public void sendReport(ReportType type, Message msg, int sender, int receiver) {
        Report report = new Report(type, sender, receiver, msg);
        // Send report to network.
        Network.network.sendReport(report);
    }



    public void addAdjacentNode(Node node){
        adjacent.add(node);
    }
}
