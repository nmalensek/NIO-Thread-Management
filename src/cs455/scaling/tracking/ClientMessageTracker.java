package cs455.scaling.tracking;

import cs455.scaling.client.Client;

import java.time.LocalDateTime;

public class ClientMessageTracker extends Thread {

    private Client client;
    private int currentSentMessages;
    private int currentReceivedMessages;

    private int previousSentMessages;
    private int previousReceivedMessages;

    public ClientMessageTracker(Client client) {
        this.client = client;
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(10000);
                reportClientThroughput();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void reportClientThroughput() {
        client.copyTrackers();
        removeOldCounts();
        archiveCounts();
        printMessagingRate(currentSentMessages, currentReceivedMessages);
    }

    private void removeOldCounts() {
        currentSentMessages = currentSentMessages - previousSentMessages;
        currentReceivedMessages = currentReceivedMessages - previousReceivedMessages;
    }

    private void archiveCounts() {
        previousSentMessages = currentSentMessages;
        previousReceivedMessages = currentReceivedMessages;
    }

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
}
