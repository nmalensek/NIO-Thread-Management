package cs455.scaling.test;

import cs455.scaling.threadpool.ThreadPool;
import cs455.scaling.threadpool.ThreadPoolManager;

public class ThreadPoolTest {
    private static int taskCounter;
    private ThreadPool testThreadPool = ThreadPool.getInstance();
    private ThreadPoolManager testTPM = ThreadPoolManager.getInstance();


    private void start() {
        testThreadPool.setPoolSize(10);
        testThreadPool.createThreads();
        testTPM.start();
        addTasksToList();
    }

    public synchronized void incrementTaskCounter() {
        taskCounter++;
        System.out.println(taskCounter);
    }

    private void addSingleMission() {
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
