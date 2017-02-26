package cs455.scaling.threadpool;

import cs455.scaling.server.Worker;
import cs455.scaling.tasks.Task;

import java.io.IOException;
import java.util.LinkedList;


public class ThreadPoolManager extends Thread {
    private static final LinkedList<Task> TASK_LINKED_LIST = new LinkedList<>();
    private ThreadPool threadPool = ThreadPool.getInstance();
    private LinkedList<Worker> availableWorkers = threadPool.getWorkers();
    private static final ThreadPoolManager threadPoolManager = new ThreadPoolManager();
    public static final Object workerAvailableObject = new Object();
    public static final Object taskAvailableObject = new Object();

    public static ThreadPoolManager getInstance() {
        return threadPoolManager;
    }

    public void run() {
        while (true) {
            while (!TASK_LINKED_LIST.isEmpty() && !availableWorkers.isEmpty()) {
                try {
                    giveTaskToWorker();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (TASK_LINKED_LIST.isEmpty()) {
                try {
                    synchronized (taskAvailableObject) {
                        taskAvailableObject.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if (availableWorkers.isEmpty()) {
                try {
                    synchronized (workerAvailableObject) {
                        workerAvailableObject.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private synchronized void giveTaskToWorker() throws IOException, InterruptedException {
        synchronized (taskAvailableObject) {
            Worker worker = availableWorkers.remove();
            Task task = TASK_LINKED_LIST.remove();
            worker.setTask(task);
            taskAvailableObject.notifyAll();
        }
    }

    public synchronized void addWorker(Worker worker) {
        availableWorkers.add(worker);
    }

    public synchronized void addTask(Task task) {
        synchronized (taskAvailableObject) {
            TASK_LINKED_LIST.add(task);
            taskAvailableObject.notifyAll();
        }
    }

    public void addThreadsToPool(int threadsToAdd) {
        threadPool.setPoolSize(threadsToAdd);
        threadPool.createThreads();
    }


}
