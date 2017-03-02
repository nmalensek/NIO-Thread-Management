package cs455.scaling.tasks.common;

import cs455.scaling.hash.ComputeHash;
import cs455.scaling.server.Server;
import cs455.scaling.tasks.server.ServerWrite;
import cs455.scaling.threadpool.ThreadPoolManager;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.Map;

public class HashMessage implements Task {

    private byte[] bytesToSend;
    private Map<SelectionKey, byte[]> readyMessages;
    private SelectionKey key;
    private Server server;

    public HashMessage(byte[] bytesToSend, Map<SelectionKey, byte[]> readyMessages, SelectionKey key, Server server) {
        this.bytesToSend = bytesToSend;
        this.readyMessages = readyMessages;
        this.key = key;
        this.server = server;
    }

    public synchronized void perform() throws IOException {
        byte[] bytesToSend = prepareReply(this.bytesToSend);
        ServerWrite write = new ServerWrite(key, bytesToSend, server);
        ThreadPoolManager.getInstance().addTask(write);
    }

    private byte[] prepareReply(byte[] messageFromClient) {
//        System.out.println(ComputeHash.SHA1FromBytes(messageFromClient));
        return ComputeHash.SHA1FromBytes(messageFromClient).getBytes();
    }
}
