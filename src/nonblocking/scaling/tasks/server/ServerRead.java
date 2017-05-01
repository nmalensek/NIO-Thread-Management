package nonblocking.scaling.tasks.server;

import nonblocking.scaling.server.KeyBuffers;
import nonblocking.scaling.server.Server;
import nonblocking.scaling.tasks.common.HashMessage;
import nonblocking.scaling.tasks.common.Task;
import nonblocking.scaling.threadpool.ThreadPoolManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;

/**
 * Code adapted from code given by instructor during lab help session:
 * http://www.cs.colostate.edu/~cs455/lectures/CS455-HelpSession5.pdf
 */

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

    /**
     * Reads incoming message from client. Method reads while the ByteBuffer has bytes to read remaining
     * in the SocketChannel and the connection is still active. The "read" action is then marked as complete
     * by removing the R character from the key's pending actions list. Checks are in place to ensure the server
     * reads the entire 8 kb message from the client (see comments below).
     * @throws IOException
     */

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
            channel.close();
            ThreadPoolManager.getInstance().addTask(new PrintDisconnect());
            keyActions.get(key).remove(Character.valueOf('R'));
            server.decrementConnectionCount();
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
            int dataStored = byteBuffer.limit(); //ensures all bytes in packet are read even if they're read in chunks
            byte[] byteCopy = new byte[dataStored];
            System.arraycopy(byteBuffer.array(), 0, byteCopy, 0, dataStored); //limit is total message size, ensures everything will be copied correctly!
            HashMessage hashMessage = new HashMessage(byteCopy, readyMessages, key, server);
            ThreadPoolManager.getInstance().addTask(hashMessage);
            server.incrementMessagesReceived();
            keyActions.get(key).remove(Character.valueOf('R'));
//        key.interestOps(SelectionKey.OP_WRITE);
        }
    }
}