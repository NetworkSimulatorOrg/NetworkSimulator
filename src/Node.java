import java.util.ArrayList;
import java.util.List;

public class Node {
    private static int count = 0;
    private final String id;
    private final int idNumber;
    private Boolean sending;
    private Integer receivingCount;
    protected final List<Node> adjacent;
    protected DeltaList sleepList;
    protected int propagationRate;
    protected int distance, longestDistance;
    protected Thread receivingThread = null;
    protected Thread sendingThread = null;
    protected volatile boolean receivingRunning = false;
    protected volatile boolean sendingRunning = false;

    public Node(String id, int propagationRate, int distance) {
        this.id = id;
        this.idNumber = count;
        count++;
        this.sending = false;
        this.receivingCount = 0;
        this.adjacent = new ArrayList<>();
        this.sleepList = new DeltaList();
        this.propagationRate = propagationRate;
        this.distance = distance;
        this.longestDistance = distance;
    }

    public void run() {
        if(receivingThread != null) {
            receivingThread.start();
        }
        if(sendingThread != null) {
            sendingThread.start();
        }
    }

    public void terminateThreads() {
        if(receivingThread != null) {
            receivingThread.interrupt();
        }
        if(sendingThread != null) {
            sendingThread.interrupt();
        }
        receivingRunning = false;
        sendingRunning = false;
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

    public int getIdNumber() {
        return idNumber;
    }

    public static void resetIdNumbers() {
        count = 0;
    }

    // This is only used for ComputeNode's
    public void sendingDelay(Message msg) throws InterruptedException {
        // Wait until the message has reached every node
        synchronized(msg){
            msg.wait();
        }
    }

    protected Message propagationDelay() throws InterruptedException {
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
            //System.out.println("Node " + getId() + " sending " + msg.getPayload() + " to node " + recv.getId());

            // Set the timestamp at which the message would be received for the node that receives it
            //msg.setTimestamp((long) (propagationRate * distance) + System.currentTimeMillis(), receiver);
            recv.sleepList.push((long) (propagationRate * distance) + System.currentTimeMillis(), msg);
        }
    }



    public void addAdjacentNode(Node node){
        adjacent.add(node);
    }
}
