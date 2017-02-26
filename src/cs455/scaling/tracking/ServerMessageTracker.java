package cs455.scaling.tracking;

import cs455.scaling.server.Server;

import java.time.LocalDateTime;

public class ServerMessageTracker extends Thread {

    private Server server;
    private int currentSentMessages;
    private int currentReceivedMessages;
    private int currentActiveConnections;

    private int previousSentMessages;
    private int previousReceivedMessages;

    public ServerMessageTracker(Server server) {
        this.server = server;
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(5000);
                reportServerThroughput();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void reportServerThroughput() {
        server.copyTrackers();
        removeOldCounts();
        archiveCounts();
        printThroughputMessage(calculateThroughput());
    }

    private void removeOldCounts() {
        currentSentMessages = currentSentMessages - previousSentMessages;
        currentReceivedMessages = currentReceivedMessages - previousReceivedMessages;
    }

    private void archiveCounts() {
        previousSentMessages = currentSentMessages;
        previousReceivedMessages = currentReceivedMessages;
    }

    public int calculateThroughput() {
        int throughput = (currentSentMessages + currentReceivedMessages) / 5;
        return throughput;
    }

    public void printThroughputMessage(int throughput) {
        String throughputMessage = "";
        throughputMessage += LocalDateTime.now().toString() + " ";
        throughputMessage += "Current Server Throughput: " + throughput + " messages/s ";
        throughputMessage += "Active Client Connections: " + currentActiveConnections;
        System.out.println(throughputMessage);
    }

    public void setCurrentSentMessages(int currentSentMessages) {
        this.currentSentMessages = currentSentMessages;
    }

    public void setCurrentReceivedMessages(int currentReceivedMessages) {
        this.currentReceivedMessages = currentReceivedMessages;
    }

    public void setCurrentActiveConnections(int currentActiveConnections) {
        this.currentActiveConnections = currentActiveConnections;
    }
}
