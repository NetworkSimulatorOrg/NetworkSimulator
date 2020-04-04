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
        for(int i = 0; i < list.size(); i++) {
            if(time > list.get(i)) {
                time -= list.get(i);
            } else {
                list.add(i, time);
                list.set(i + 1, list.get(i+1) - time);
            }
        }
    }

    // Waits until first item has expired.
    public void sleep() throws InterruptedException {
        if(list.size() > 0) {
            Thread.sleep(list.get(0));
            synchronized (list) {
                list.remove(0);
            }
        }

    }
}
