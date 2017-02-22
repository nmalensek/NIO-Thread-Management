package cs455.scaling.test;

import cs455.scaling.tasks.Task;
import cs455.scaling.threadpool.ThreadPool;
import cs455.scaling.threadpool.ThreadPoolManager;

import java.util.concurrent.ThreadLocalRandom;

public class ThreadPoolTest {

    private ThreadPool testThreadPool = ThreadPool.getInstance();
    private ThreadPoolManager testTPM = ThreadPoolManager.getInstance();
    private static int taskCounter;
    private Long testLong = new Long(ThreadLocalRandom.current().nextInt(-2147483648, 2147483647));
    private Long testLong2 = new Long(34);
    private Byte testByte = new Byte(Byte.MAX_VALUE);
    private static byte[] testArray;

    private void start() {
        testThreadPool.setPoolSize(10);
        testThreadPool.createThreads();
        addMissionsToList();
        while (true) {
            try {
                Thread.sleep(2000);
                addSingleMission();
                addSingleMission();
                addSingleMission();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void printPrimitiveSizes() {
        System.out.println(testLong.SIZE/8);
        System.out.println(testLong2.SIZE/8);
        System.out.println(testByte.SIZE/8);
    }

    private byte[] prepareMessage() {
        byte[] byteArray = new byte[8000];
        for (int i = 0; i < byteArray.length-1; i++) {
            byteArray[i] = (byte) ThreadLocalRandom.current().nextInt(127);
        }
        return byteArray;
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
//        threadPoolTest.printPrimitiveSizes();
//        threadPoolTest.start();
        testArray = threadPoolTest.prepareMessage();
        System.out.println(testArray.length);
    }

}
