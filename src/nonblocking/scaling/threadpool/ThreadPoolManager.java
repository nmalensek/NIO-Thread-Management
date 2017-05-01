package nonblocking.scaling.threadpool;

import nonblocking.scaling.tasks.common.Task;

import java.util.LinkedList;


public class ThreadPoolManager extends Thread {
    private ThreadPool threadPool = ThreadPool.getInstance();
    private static final LinkedList<Task> TASK_LINKED_LIST = new LinkedList<>();
    private static final ThreadPoolManager threadPoolManager = new ThreadPoolManager();

    public static ThreadPoolManager getInstance() {
        return threadPoolManager;
    }

    /**
     * ThreadPoolManager implemented as its own thread so server is always
     * free to handle new connections.
     */

    public void run() {
        while (true) {
            //handle incoming interactions
        }
    }

    /**
     * Notifies all waiting threads that a new task is available to work on.
     * Is called every time a task is created.
     * @param task Task that needs to be performed.
     */

    public synchronized void addTask(Task task) {
        TASK_LINKED_LIST.add(task);
        notifyAll();
    }

    /**
     * Removes a completed task from the pending tasks list, effectively marking
     * it as complete. If there are no new tasks, the thread waits.
     * @return Task that the thread will now be executing.
     */

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

    /**
     * Called to create thread pool when server is initialized.
     * @param threadsToAdd Desired thread pool size.
     */

    public void addThreadsToPool(int threadsToAdd) {
        threadPool.setPoolSize(threadsToAdd);
        threadPool.createThreads();
    }


}
