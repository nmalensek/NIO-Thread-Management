package cs455.scaling.tasks;

import cs455.scaling.server.KeyBuffers;
import cs455.scaling.server.Server;
import cs455.scaling.threadpool.ThreadPoolManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;

public class ServerRead implements Task {

    private SelectionKey key;
    private Server server;
    private Map<SelectionKey, byte[]> readyMessages;
    private Map<SelectionKey, List<Character>> keyActions;

    public ServerRead(SelectionKey key, Server server,
                      Map<SelectionKey, byte[]> readyMessages, Map<SelectionKey, List<Character>> keyActions) {
        this.key = key;
        this.server = server;
        this.readyMessages = readyMessages;
        this.keyActions = keyActions;
    }

    public synchronized void perform() throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        KeyBuffers keyBuffers = (KeyBuffers) key.attachment();
        ByteBuffer byteBuffer = keyBuffers.getReadBuffer();
        int read = 0;
        byteBuffer.clear();
        try {
            while (byteBuffer.hasRemaining() && read != -1) {
                read = channel.read(byteBuffer);
            }

        } catch (IOException e) {
            System.out.println("IO Error, connection closed");
            channel.close();
            return;
        }

        if (read == -1) {
            //connection terminated by client
            channel.close();
            //threads notified to keep going
            ThreadPoolManager.getInstance().addTask(new PrintDisconnect());
            keyActions.get(key).remove(Character.valueOf('R'));
            server.decrementConnectionCount();
            return;
        } else {
            byte[] byteCopy = new byte[read];
            System.arraycopy(byteBuffer.array(), 0, byteCopy, 0, read);
            HashMessage hashMessage = new HashMessage(byteCopy, readyMessages, key, server);
            ThreadPoolManager.getInstance().addTask(hashMessage);
            server.incrementMessagesReceived();
            keyActions.get(key).remove(Character.valueOf('R'));
//        key.interestOps(SelectionKey.OP_WRITE);
        }
    }
}