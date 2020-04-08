public class Report {
    private ReportType type;
    private int sender;
    private int receiver;
    private Message msg;

    public Report(ReportType type, int sender, int receiver, Message msg) {
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.msg = msg;
    }

    public ReportType getType(){
        return type;
    }

    public int getSender(){
        return sender;
    }

    public int getReceiver(){
        return receiver;
    }

    public Message getMsg(){
        return msg;
    }

    public void logReport(){
        if (type == ReportType.Successful){
            logSuccessful();
        }
        else{
            logCollision();
        }
    }

    private void logSuccessful(){
        System.out.println("Node " + receiver + " received the message " + msg.getPayload() + ".");
    }

    private void logCollision(){
        System.out.println("Collision: " + msg.getPayload() + " collided when going from Node " + msg.getLastSender() + " to Node " + receiver);
    }
}
