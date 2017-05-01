package nonblocking.scaling.threadpool;

import java.io.IOException;

public class Worker extends Thread {

    private ThreadPoolManager threadPoolManager = ThreadPoolManager.getInstance();

    /**
     * Worker thread, constantly removes tasks and performs them as long
     * as there's a task in the queue.
     */

    public void run() {
        while (true) {
            try {
                threadPoolManager.removeTask().perform();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
