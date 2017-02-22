package cs455.scaling.server;

import cs455.scaling.tasks.Task;
import cs455.scaling.threadpool.ThreadPool;
import cs455.scaling.threadpool.ThreadPoolManager;

import java.io.IOException;
import java.util.List;

public class Worker extends Thread {

    private ThreadPoolManager threadPoolManager = ThreadPoolManager.getInstance();
    private ThreadPool threadPool = ThreadPool.getInstance();
    private List<Worker> availableWorkers = threadPool.getWorkers();

    public void run() {
        while (true) {
            try {
                Task task = threadPoolManager.removeFirstTask();
                availableWorkers.remove(this);
                task.perform();
                availableWorkers.add(this);
                synchronized (threadPoolManager.objectToSynchronizeOn) {
                    threadPoolManager.objectToSynchronizeOn.notify();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
