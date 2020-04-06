import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

public class DeltaList {
    private List<Long> list;

    public DeltaList() {
        list = Collections.synchronizedList(new ArrayList<>());
    }

    public synchronized void push(long time) {
        // Push the time that 
        for(int i = 0; i < list.size(); i++) {
            if(time < list.get(i)) {
                list.add(i, time);
            }
        }
    }

    // Waits until first item has expired.
    public void sleep() throws InterruptedException {
        if(list.size() > 0) {
            Thread.sleep(list.get(0) - System.currentTimeMillis());
            synchronized (list) {
                list.remove(0);
            }
        }

    }
}
