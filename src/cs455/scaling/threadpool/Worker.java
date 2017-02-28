package cs455.scaling.threadpool;

import cs455.scaling.tasks.Task;

import java.io.IOException;

public class Worker extends Thread {

    private ThreadPoolManager threadPoolManager = ThreadPoolManager.getInstance();
    private boolean hasTask = false;
    private Task task;

    public void run() {
        while (true) {
            try {
                threadPoolManager.removeTask().perform();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setTask(Task task) {
        this.task = task;
        hasTask = true;
    }
}
