package cs455.scaling.threadpool;

import cs455.scaling.server.Worker;
import cs455.scaling.tasks.Task;

import java.util.LinkedList;
import java.util.List;

public class ThreadPoolManager {
    private static final LinkedList<Task> TASK_LINKED_LIST = new LinkedList<>();
    private ThreadPool threadPool = ThreadPool.getInstance();
    private List<Worker> availableWorkers = threadPool.getWorkers();
    private static final ThreadPoolManager threadPoolManager = new ThreadPoolManager();
    public static Object objectToSynchronizeOn = new Object();

    public static ThreadPoolManager getInstance() {
        return threadPoolManager;
    }

    public synchronized void addTask(Task task) {
        synchronized (objectToSynchronizeOn) {
            while (availableWorkers.isEmpty()) {
                try {
                    objectToSynchronizeOn.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        TASK_LINKED_LIST.add(task);
        notifyAll();
    }

    public synchronized Task removeFirstTask() throws InterruptedException {
        while (TASK_LINKED_LIST.isEmpty()) {
            wait();
        }
        return TASK_LINKED_LIST.remove();
    }

    public void addThreadsToPool(int threadsToAdd) {
        threadPool.setPoolSize(threadsToAdd);
        threadPool.createThreads();
    }


}
