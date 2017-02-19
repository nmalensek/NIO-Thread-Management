package cs455.scaling.test;

import cs455.scaling.missions.Mission;

public class TestSleep implements Mission {

    private ThreadPoolTest threadPoolTest;

    public TestSleep(ThreadPoolTest threadPoolTest) {
        this.threadPoolTest = threadPoolTest;
    }

    public void perform() {
        System.out.println("ZzzZZzz...");
        try {
            Thread.sleep(5000);
            System.out.println("awake!");
            threadPoolTest.incrementTaskCounter();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
