package cs455.scaling.threadpool;

import cs455.scaling.tasks.Task;

import java.io.IOException;

public class Worker extends Thread {

    private ThreadPoolManager threadPoolManager = ThreadPoolManager.getInstance();

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
