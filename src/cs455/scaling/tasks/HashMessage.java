package cs455.scaling.tasks;

import cs455.scaling.hash.ComputeHash;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.Map;

public class HashMessage implements Task {

    private byte[] bytesToSend;
    private Map<SelectionKey, byte[]> readyMessages;
    private SelectionKey key;

    public HashMessage(byte[] bytesToSend, Map<SelectionKey, byte[]> readyMessages, SelectionKey key) {
        this.bytesToSend = bytesToSend;
        this.readyMessages = readyMessages;
        this.key = key;
    }

    public synchronized void perform() throws IOException {
        byte[] bytesToSend = prepareReply(this.bytesToSend);
        readyMessages.put(key, bytesToSend);
    }

    private byte[] prepareReply(byte[] messageFromClient) {
//        System.out.println(ComputeHash.SHA1FromBytes(messageFromClient));
        return ComputeHash.SHA1FromBytes(messageFromClient).getBytes();
    }
}
