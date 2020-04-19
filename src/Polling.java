import java.util.concurrent.SynchronousQueue;

public class Polling implements Protocol {
    private final SynchronousQueue<Message> messages;
    private Thread synchronizing;
    private volatile boolean synchronizingRunning;

    /* Polling structure
     * sending node waits for permission.
     * Notify one at a time
     * Sync thread waits while sending
     * sending node notifies sync thread when done
     *
     * If no object notified, keep on trying.
     */

    public Polling() {
        messages = new SynchronousQueue<>();

        synchronizing = new Thread(this::synchronizeThread);
    }

    private void synchronizeThread() {
        // After waiting
        synchronizingRunning = true;
        while (synchronizingRunning) {
            try {
                var msg = messages.take();
                Protocol.sendMsgHelper(Network.network.getNodeById(msg.getSender()), msg);

                synchronized (messages) {
                    messages.notify();
                }

            } catch (InterruptedException e) {
                synchronizingRunning = false;
            }
        }
    }

    @Override
    public ProtocolState sendMsg(Node node, Message msg) throws InterruptedException {
        messages.put(msg);

        synchronized (messages) {
            messages.wait();
        }

        return ProtocolState.Success;
    }

    @Override
    public ProtocolState recvMsg(Node node, Message msg) {
        Protocol.recvMsgHelper(node, msg);

        // Check for corruption and collision
        if (msg.isCorrupt())
            // Aloha does not stop sending the outgoing message. Do not end the sending delay.
            return ProtocolState.Failure;
        return ProtocolState.Success;
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
