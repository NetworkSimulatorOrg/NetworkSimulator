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

    public void logReport(CSVWriter writer){
        if (type == ReportType.Successful){
            logSuccessful(writer);
        }
        else{
            logCollision(writer);
        }
    }

    private void logSuccessful(CSVWriter writer){
        System.out.println("Node " + receiver + " received the message " + msg.getPayload() + ".");
        writer.appendDataAsLine("Success", Integer.toString(sender), Integer.toString(receiver), Integer.toString(msg.getSequenceNumber()), msg.getPayload());
    }

    private void logCollision(CSVWriter writer){
        System.out.println("Collision: " + msg.getPayload() + " collided when going from Node " + msg.getLastSender() + " to Node " + receiver);
        writer.appendDataAsLine("Collision", Integer.toString(sender), Integer.toString(receiver), Integer.toString(msg.getSequenceNumber()), msg.getPayload());
    }
}
