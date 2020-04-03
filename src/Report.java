public class Report {
    public ReportType type;
    public int sender;
    public int receiver;
    public Message msg;

    public Report(ReportType type, int sender, int receiver, Message msg) {
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.msg = msg;
    }
}
