package cs455.scaling.test;

import cs455.scaling.missions.Mission;

public class TestPrint implements Mission {

    private ThreadPoolTest threadPoolTest;

    public TestPrint(ThreadPoolTest threadPoolTest) {
        this.threadPoolTest = threadPoolTest;
    }


    public void perform() {
        System.out.println("mission complete!");
        threadPoolTest.incrementTaskCounter();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
