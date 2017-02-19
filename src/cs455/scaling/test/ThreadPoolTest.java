package cs455.scaling.test;

import cs455.scaling.server.Mission;
import cs455.scaling.server.ThreadPool;
import cs455.scaling.server.ThreadPoolManager;

public class ThreadPoolTest {

    private ThreadPool testThreadPool = ThreadPool.getInstance();
    private ThreadPoolManager testTPM = ThreadPoolManager.getInstance();
    private static int taskCounter;

    private void start() {
        testThreadPool.setPoolSize(10);
        addMissionsToList();
        testThreadPool.createThreads();
        while (true) {
            try {
                Thread.sleep(5000);
                addMissionsToList();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void incrementTaskCounter() {
        taskCounter++;
        System.out.println(taskCounter);
    }

    private void addMissionsToList() {
        for (Mission mission : missions) {
            testTPM.addTask(mission);
        }
    }

    Mission[] missions = {
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
