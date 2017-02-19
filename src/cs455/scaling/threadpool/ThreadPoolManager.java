package cs455.scaling.threadpool;

import cs455.scaling.missions.Mission;

import java.util.LinkedList;

public class ThreadPoolManager {
    private static final LinkedList<Mission> taskList = new LinkedList<>();
    private ThreadPool threadPool = ThreadPool.getInstance();
    private static final ThreadPoolManager threadPoolManager = new ThreadPoolManager();

    public static ThreadPoolManager getInstance() { return threadPoolManager; }

    public synchronized void addTask(Mission mission) {
            taskList.add(mission);
            notifyAll();
    }

    public synchronized Mission removeFirstTask() throws InterruptedException {
            while (taskList.size() == 0) {
                wait();
            }
            return taskList.remove();
    }

    public void addThreadsToPool(int threadsToAdd) {
        threadPool.setPoolSize(threadsToAdd);
        threadPool.createThreads();
    }


}
