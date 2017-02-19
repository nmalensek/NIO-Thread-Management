package cs455.scaling.server;

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
