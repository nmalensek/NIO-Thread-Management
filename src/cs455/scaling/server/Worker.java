package cs455.scaling.server;

import cs455.scaling.missions.Mission;
import cs455.scaling.threadpool.ThreadPoolManager;

public class Worker extends Thread {

    private ThreadPoolManager threadPoolManager = ThreadPoolManager.getInstance();

    public Worker() {
    }

    public void run() {
        while (true) {
            try {
                Mission mission = threadPoolManager.removeFirstTask();
                mission.perform();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
