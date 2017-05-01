package nonblocking.scaling.tracking;

import nonblocking.scaling.client.Client;

import java.time.LocalDateTime;

public class ClientMessageTracker extends Thread {

    private Client client;
    private int currentSentMessages;
    private int currentReceivedMessages;
    private boolean running = true;

    public ClientMessageTracker(Client client) {
        this.client = client;
    }

    /**
     * Prints message throughput every 10 seconds.
     */
    public void run() {
        while (running) {
            try {
                Thread.sleep(10000);
                reportClientThroughput();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void reportClientThroughput() {
        client.copyAndResetTrackers();
        printMessagingRate(currentSentMessages, currentReceivedMessages);
    }

    /**
     * Constructs string of client's sent and received messages for the current timestamp
     * @param sent number of messages sent to the server
     * @param received number of messages received from the server
     */

    public void printMessagingRate(int sent, int received) {
        String throughputMessage = "";
        throughputMessage += LocalDateTime.now().toString() + " ";
        throughputMessage += "Total sent count: " + sent + ", ";
        throughputMessage += "Total received count: " + received;
        System.out.println(throughputMessage);
    }

    public void setCurrentSentMessages(int currentSentMessages) {
        this.currentSentMessages = currentSentMessages;
    }

    public void setCurrentReceivedMessages(int currentReceivedMessages) {
        this.currentReceivedMessages = currentReceivedMessages;
    }

    /**
     * Shuts down this class's thread. Currently only called by the client if the server disconnects.
     * Thread will finish its current execution before stopping, but that's fine in this case because
     * it's just printing statements.
     */

    public void shutdown() {
        running = false;
    }
}
