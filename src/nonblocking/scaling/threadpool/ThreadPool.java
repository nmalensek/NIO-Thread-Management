package nonblocking.scaling.threadpool;

import java.util.LinkedList;

public class ThreadPool {

    private int poolSize;
    private static final ThreadPool instance = new ThreadPool();
    private final LinkedList<Worker> workers = new LinkedList<>();

    private ThreadPool() {}

    public static ThreadPool getInstance() { return instance; }

    /**
     * Sets thread pool size.
     * @param size Desired thread pool size.
     */

    public void setPoolSize(int size) { poolSize = size; }

    /**
     * Creates a thread pool of specified size and starts every thread in the pool.
     */

    public void createThreads() {
        for (int i = 0; i < poolSize; i++) {
            workers.add(new Worker());
        }

        for (Worker worker : workers) {
            worker.start();
        }
    }

}
