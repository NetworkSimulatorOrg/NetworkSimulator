public class SlottedAloha extends Aloha implements Protocol{

    private Object sync;
    private Thread synchronizing;
    private volatile boolean synchronizingRunning;

    public SlottedAloha(){
        sync = new Object();

        synchronizing = new Thread(this::synchronizeThread);
    }

    private void synchronizeThread() {
        // After waiting
        synchronizingRunning = true;
        while(synchronizingRunning) {
            synchronized(sync){
                sync.notifyAll();
            }

            try {
                // Allow a little extra time in case of congestion
                Thread.sleep((int)(Network.longestDistance * Network.propagationRate * 1.2));
            } catch (/*Interrupted*/Exception e) {
                if(!(e instanceof InterruptedException)) {
                    e.printStackTrace();
                }
                synchronizingRunning = false;
            }
        }
        if(Network.logToConsole) {
            System.out.println("Protocol terminating synchronizeThread");
        }
    }

    public ProtocolState sendMsg(Node node, Message msg) throws InterruptedException {
        node.startSendingTimestamp = System.currentTimeMillis();
        
        // Keep resending the message until there is no collision
        while (true){

            // Wait until the next frame starts
            synchronized (sync){
                sync.wait();
            }

            Protocol.sendMsgHelper(node, msg);

            // Check if this message collided
            if (msg.isCorrupt()){
                // Resend the message at some future time.
                int delay = (int) (Math.random() * Network.computeNodeCount * node.longestDistance * node.propagationRate);
                if(Network.logToConsole) {
                    System.out.println("Node " + node.getId() + " will be delayed for " + delay + " ms.");
                }
                Thread.sleep(delay);
                msg.prepareForRetransmission();
            }
            else{
                break;
            }

        }

        // No check for state of line, so always return success.
        return ProtocolState.Success;

        //return super.sendMsg(node, msg);
    }

    public ProtocolState recvMsg(Node node, Message msg){
        return super.recvMsg(node, msg);
    }

    @Override
    public ProtocolState run() {
        synchronizing.start();
        return ProtocolState.Success;
    }

    @Override
    public ProtocolState terminateThreads() {
        if(synchronizing != null) {
            synchronizing.interrupt();
        }
        synchronizingRunning = false;
        return ProtocolState.Success;
    }
}