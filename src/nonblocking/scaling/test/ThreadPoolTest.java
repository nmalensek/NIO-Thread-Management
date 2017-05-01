package nonblocking.scaling.test;

import nonblocking.scaling.threadpool.ThreadPool;
import nonblocking.scaling.threadpool.ThreadPoolManager;

/**
 * Tests thread pool implementation by loading thread pool with junk tasks.
 */

public class ThreadPoolTest {
    private static int taskCounter;
    private ThreadPool testThreadPool = ThreadPool.getInstance();
    private ThreadPoolManager testTPM = ThreadPoolManager.getInstance();


    private void start() {
        testThreadPool.setPoolSize(10);
        System.out.println("creating threads");
        testThreadPool.createThreads();
        testTPM.start();
        addTasksToList();
    }

    public synchronized void incrementTaskCounter() {
        taskCounter++;
        System.out.println(taskCounter);
    }

    private void addSingleTask() {
        testTPM.addTask(new TestPrint(this));
    }

    private void addTasksToList() {
        for (int i = 0; i < 1000; i++) {
            testTPM.addTask(new TestPrint(this));
        }
    }

    public static void main(String[] args) {
        ThreadPoolTest threadPoolTest = new ThreadPoolTest();
        threadPoolTest.start();
    }

}
