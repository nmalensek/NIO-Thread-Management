package cs455.scaling.threadpool;

import cs455.scaling.server.Worker;

import java.util.ArrayList;
import java.util.List;

public class ThreadPool {

    private int poolSize;
    private static final ThreadPool instance = new ThreadPool();
    private final List<Worker> workers = new ArrayList<>();

    private ThreadPool() {}

    public static ThreadPool getInstance() { return instance; }

    public List<Worker> getWorkers() { return workers; }
    public void setPoolSize(int size) { poolSize = size; }

    public void createThreads() {
        for (int i = 0; i < poolSize; i++) {
            workers.add(new Worker());
        }

        for (Worker worker : workers) {
            worker.start();
        }
    }

}
