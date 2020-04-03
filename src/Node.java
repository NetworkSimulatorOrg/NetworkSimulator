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

    protected void propagationDelay() {
        try {
            Thread.sleep(((long) (propagationRate * distance)));
        } catch(InterruptedException e) {
            System.out.print(e);
        }
    }

    public Node getAdjacentNodeByID(int id) {
        for(int i = 0; i < adjacent.size(); i++) {
            if(adjacent.get(i).getId() == id) {
                return adjacent.get(i);
            }
        }
        return null;
    }

    public void sendMsg(Message msg, int receiver) {
        Node recv = getAdjacentNodeByID(receiver);

        if(recv != null) {
            // pass message to recv
        }
    }

    public void recvMsg(Message msg, int sender) {
        try {
            messages.put(msg);
        } catch(InterruptedException e) {
            System.out.println(e);
        }
    }

    public void sendReport(ReportType type, Message msg, int sender, int receiver) {
        Report report = new Report(type, sender, receiver, msg);
        // Send report to network.
    }
}
