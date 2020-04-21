public class ConnectNode extends Node {

    public ConnectNode(String id, int propagationRate, int distance) {
        super(id, propagationRate, distance);

        // Create necessary threads
        receivingThread = new Thread(this::recvMsgThread);
    }

    private void sendMsg(Message msg) {
        // Send the message to all adjacent nodes besides the one that sent it,
        var lastSender = msg.getLastSender(id);

        StringBuilder builder = new StringBuilder();
        if(Network.logToConsole) {
            builder.append("Connect ");
            builder.append(getId());
            builder.append(": Repeating message\n");
            builder.append(msg.toString("\t", getId()));
        }

        for(Node node : adjacent) {
            if (!node.getId().equals(lastSender)) {
                super.sendMsg(msg, node.getId());
                if(Network.logToConsole) {
                    builder.append("\t To ");
                    builder.append(node.getId());
                    builder.append("\n");
                }
            }
        }

        if(Network.logToConsole) {
            System.out.println(builder.toString());
        }
    }

    // This is not needed. Messages actually being passed between
    // threads is done in the thread of the sending node. Only 1
    // thread is needed for a connect node.
    private void sendMsgThread() {
    }

    private void recvMsgThread() {
        Message msg = null;
        receivingRunning = true;
        while(receivingRunning) {
            // Check if a message is in the queue
            try {
                if ((msg = sleepList.sleep()) != null) {
                    // Send out message. Time delay has passed.
                    sendMsg(msg);
                }

                //Thread.sleep(delay);
            } catch(Exception e) {
                if(!(e instanceof InterruptedException)) {
                    e.printStackTrace();
                }
                receivingRunning = false;
            }
        }

        if(Network.logToConsole) {
            System.out.println("Connect Node " + getId() + " terminating recvMsgThread");
        }
    }
}
