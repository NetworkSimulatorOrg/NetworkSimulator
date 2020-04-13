import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

public class DeltaList {
    private List<Long> list;
    private List<Message> msgs;

    public DeltaList() {
        list = new ArrayList<>();
        msgs = new ArrayList<>();
    }

    public synchronized void push(long time, Message msg) {

        System.out.println(msg.getPayload() + " pushed.");

        int i = 0;

        // Push the time that 
        for(; i < list.size(); i++) {
            if(time < list.get(i)) {
                list.add(i, time);
                msgs.add(i, msg);
            }
        }

        if (list.size() == i){
            list.add(time);
            msgs.add(msg);
        }
    }

    // Waits until first item has expired.
    public synchronized Message sleep() throws InterruptedException {
        if(list.size() > 0) {
            System.out.println("Message " + msgs.get(0).getPayload() + " found in queue.");
            System.out.println("Sleeping for " + (list.get(0) - System.currentTimeMillis()) + " milliseconds");
            if (list.get(0) - System.currentTimeMillis() > 0) {
                Thread.sleep(list.get(0) - System.currentTimeMillis());
            }
            list.remove(0);
            return msgs.remove(0);
        }
        return null;
    }
}
