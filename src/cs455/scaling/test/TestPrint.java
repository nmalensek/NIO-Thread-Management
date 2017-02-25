package cs455.scaling.test;

import cs455.scaling.tasks.Task;

import java.util.concurrent.ThreadLocalRandom;

public class TestPrint implements Task {

    private ThreadPoolTest threadPoolTest;

    public TestPrint(ThreadPoolTest threadPoolTest) {
        this.threadPoolTest = threadPoolTest;
    }


    public void perform() {
        System.out.println("mission complete!");
        threadPoolTest.incrementTaskCounter();
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(5000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
