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
                performTask();
                threadPoolManager.addWorker(this);
                synchronized (threadPoolManager.workerAvailableObject) {
                    threadPoolManager.workerAvailableObject.notify();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setTask(Task task) {
        this.task = task;
        hasTask = true;
    }

    public void performTask() throws IOException, InterruptedException {
        while (!hasTask) {
            synchronized (threadPoolManager.taskAvailableObject) {
                threadPoolManager.taskAvailableObject.wait();
            }
        }
        task.perform();
        hasTask = false;
    }
}
