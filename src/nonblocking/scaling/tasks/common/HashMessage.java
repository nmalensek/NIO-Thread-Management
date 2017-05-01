package nonblocking.scaling.tasks.common;

import nonblocking.scaling.hash.ComputeHash;
import nonblocking.scaling.server.Server;
import nonblocking.scaling.tasks.server.ServerWrite;
import nonblocking.scaling.threadpool.ThreadPoolManager;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.Map;

/**
 * Computes hash of a message using the ComputeHash class. Hash computation
 * is implemented as a task and carried out by worker threads.
 */

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
