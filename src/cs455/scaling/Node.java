package cs455.scaling;

public interface Node {

    void incrementMessagesSent();
    void incrementMessagesReceived();
    void incrementConnectionCount();
    void decrementConnectionCount();
}
