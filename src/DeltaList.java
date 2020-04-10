import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

public class DeltaList {
    private List<Long> list;
    private List<Message> msgs;

    public DeltaList() {
        list = Collections.synchronizedList(new ArrayList<>());
        msgs = Collections.synchronizedList(new ArrayList<>());
    }

    public synchronized void push(long time, Message msg) {
        // Push the time that 
        for(int i = 0; i < list.size(); i++) {
            if(time < list.get(i)) {
                list.add(i, time);
                msgs.add(i, msg);
            }
        }
    }

    // Waits until first item has expired.
    public synchronized Message sleep() {
        if(list.size() > 0) {
            try {
                Thread.sleep(list.get(0) - System.currentTimeMillis());
            } catch (InterruptedException e) {
                System.out.println(e);
            }
            list.remove(0);
            return msgs.remove(0);
        }
        return null;
    }
}
