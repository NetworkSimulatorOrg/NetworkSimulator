public class Report {
    private ReportType type;
    private String sender;
    private String receiver;
    private Message msg;

    public Report(ReportType type, String sender, String receiver, Message msg) {
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.msg = msg;
    }

    public ReportType getType(){
        return type;
    }

    public String getSender(){
        return sender;
    }

    public String getReceiver(){
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
        writer.appendDataAsLine("Success", sender, receiver, Integer.toString(msg.getSequenceNumber()), "\"" + msg.getPayload() + "\"");
    }

    private void logCollision(CSVWriter writer){
        /* 
         * TODO: Check if the message + sequence number combination has already been logged.
         * If it has been logged as a success, then overwrite it to a failure.
         */

        System.out.println("Collision: " + msg.getPayload() + " collided when going from Node " + msg.getLastSender() + " to Node " + receiver);
        writer.appendDataAsLine("Collision", sender, receiver, Integer.toString(msg.getSequenceNumber()), "\"" + msg.getPayload() + "\"");
    }
}
