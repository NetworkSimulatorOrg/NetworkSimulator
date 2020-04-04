import java.util.List;
import java.util.concurrent.SynchronousQueue;

public class Node {
    protected final int id;
    protected Integer state;
    protected final List<Node> adjacent;
    protected DeltaList sleepList;
    protected SynchronousQueue messages;
    protected double propagationRate;
    protected double distance;

    public Node(int id, List<Node> adjacent, double propagationRate, double distance) {
        this.id = id;
        this.adjacent = adjacent;
        this.propagationRate = propagationRate;
        this.distance = distance;
    }

    public int getState() {
        return state;
    }

    public int addReceiver() {
        synchronized (state) {
            state++;
        }
        return state;
    }

    public int removeReceiver() {
        synchronized (state) {
            state--;
        }
        return state;
    }

    public int getId() {
        return id;
    }

    protected Message propagationDelay() {
        try {
            sleepList.sleep();
            return (Message) messages.remove();
            //Thread.sleep(((long) (propagationRate * distance)));
        } catch(InterruptedException e) {
            System.out.print(e);
        }

        return null;
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
            sleepList.push(System.currentTimeMillis() + (long) (propagationRate * distance) - msg.getTimestamp());
            try {
                messages.put(msg);
            } catch(InterruptedException e) {
                System.out.println(e);
            }
        }
    }

    public Message recvMsg() {
        // Mark node receiving
        boolean collision = addReceiver() > 1;

        Message msg = propagationDelay();

        // Test at end
        collision = collision || removeReceiver() > 0;

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
    }
}
