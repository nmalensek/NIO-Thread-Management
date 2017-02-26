package cs455.scaling.client;

public class ClientWriterThread extends Thread {

    private Client client;
    private int messageRate;

    public ClientWriterThread(Client client, int messageRate) {
        this.client = client;
        this.messageRate = messageRate;
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(1000/messageRate);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
