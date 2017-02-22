package cs455.scaling.test;

import cs455.scaling.tasks.Task;
import cs455.scaling.threadpool.ThreadPool;
import cs455.scaling.threadpool.ThreadPoolManager;

public class ThreadPoolTest {

    private ThreadPool testThreadPool = ThreadPool.getInstance();
    private ThreadPoolManager testTPM = ThreadPoolManager.getInstance();
    private static int taskCounter;

    private void start() {
        testThreadPool.setPoolSize(10);
        testThreadPool.createThreads();
        addMissionsToList();
//        while (true) {
            try {
                Thread.sleep(2000);
                addSingleMission();
                addSingleMission();
                addSingleMission();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//        }
    }

    public synchronized void incrementTaskCounter() {
        taskCounter++;
        System.out.println(taskCounter);
    }

    private void addSingleMission() {
        testTPM.addTask(new TestPrint(this));
    }

    private void addMissionsToList() {
        for (Task task : tasks) {
            testTPM.addTask(task);
        }
    }

    Task[] tasks = {
            new TestPrint(this),
            new TestPrint(this),
            new TestPrint(this),
            new TestPrint(this),
            new TestPrint(this),
            new TestPrint(this),
            new TestPrint(this),
            new TestPrint(this),
            new TestPrint(this),
            new TestPrint(this),
//            new TestSleep(this),
//            new TestSleep(this),
//            new TestSleep(this),
//            new TestSleep(this),
//            new TestSleep(this),
//            new TestSleep(this),
//            new TestSleep(this),
    };


    public static void main(String[] args) {
        ThreadPoolTest threadPoolTest = new ThreadPoolTest();
        threadPoolTest.start();
    }

}
