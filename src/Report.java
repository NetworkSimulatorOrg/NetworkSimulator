public class Report {
    private ReportType type;
    private String sender;
    private Message msg;
    private long timeTaken;

    public Report(ReportType type, String sender,  Message msg, long timeTaken) {
        this.type = type;
        this.sender = sender;
        this.msg = msg;
        this.timeTaken = timeTaken;
    }

    public ReportType getType(){
        return type;
    }

    public String getSender(){
        return sender;
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

    private void logSuccessful(CSVWriter writer) {
        if (Network.logToConsole) {
            System.out.println("Success: Node " + sender + "'s message was received by everyone.-!-!-!-!-!-!-!-!-!-!-!-!-!-!-!-!-!-!-!-!-!-!-!-!-!-!-!");
        }
        writer.appendDataAsLine("Success", sender, Integer.toString(msg.getSequenceNumber()), Integer.toString(msg.getRepeatCount()), Long.toString(timeTaken), "\"" + msg.getPayload() + "\"");
    }

    private void logCollision(CSVWriter writer) {
        if (Network.logToConsole) {
            System.out.println("Collision: " + msg.getPayload() + " collided with another message.");
        }
        writer.appendDataAsLine("Collision", sender, Integer.toString(msg.getSequenceNumber()), Integer.toString(msg.getRepeatCount()), Long.toString(timeTaken), "\"" + msg.getPayload() + "\"");
    }
}
