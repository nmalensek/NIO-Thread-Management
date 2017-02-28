package cs455.scaling.threadpool;

import cs455.scaling.tasks.Task;

import java.io.IOException;
import java.util.LinkedList;


public class ThreadPoolManager extends Thread {
    private static final LinkedList<Task> TASK_LINKED_LIST = new LinkedList<>();
    private ThreadPool threadPool = ThreadPool.getInstance();
    private static final ThreadPoolManager threadPoolManager = new ThreadPoolManager();

    public static ThreadPoolManager getInstance() {
        return threadPoolManager;
    }

    public void run() {
        while (true) {
            //handle incoming interactions
        }
    }

    public synchronized void addTask(Task task) {
        TASK_LINKED_LIST.add(task);
        notifyAll();
    }

    public synchronized Task removeTask() {
        while (TASK_LINKED_LIST.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return TASK_LINKED_LIST.remove();
    }

    public void addThreadsToPool(int threadsToAdd) {
        threadPool.setPoolSize(threadsToAdd);
        threadPool.createThreads();
    }


}
