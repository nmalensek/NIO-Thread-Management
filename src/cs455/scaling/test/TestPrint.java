package cs455.scaling.test;

import cs455.scaling.tasks.common.Task;

/**
 * Task sleeps and informs when it's done sleeping. Originally
 * slept for random amount of time to debug thread pool implementation,
 * but was removed due to assignment package restriction.
 */

public class TestPrint implements Task {

    private ThreadPoolTest threadPoolTest;

    public TestPrint(ThreadPoolTest threadPoolTest) {
        this.threadPoolTest = threadPoolTest;
    }


    public void perform() {
        System.out.println("mission complete!");
        threadPoolTest.incrementTaskCounter();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
