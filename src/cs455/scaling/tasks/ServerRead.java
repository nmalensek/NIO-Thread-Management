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
    private int bufferSize;
    private Server server;
    private ThreadPoolManager threadPoolManager = ThreadPoolManager.getInstance();
    private Map<SelectionKey, byte[]> readyMessages;
    private Map<SelectionKey, List<Character>> keyActions;

    public ServerRead(SelectionKey key, int bufferSize, Server server,
                      Map<SelectionKey, byte[]> readyMessages, Map<SelectionKey, List<Character>> keyActions) {
        this.key = key;
        this.bufferSize = bufferSize;
        this.server = server;
        this.readyMessages = readyMessages;
        this.keyActions = keyActions;
    }

    public void perform() throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        KeyBuffers keyBuffers = (KeyBuffers) key.attachment();
        ByteBuffer byteBuffer = keyBuffers.getReadBuffer();
        int read = 0;

        try {
            while (byteBuffer.hasRemaining() && read != -1) {
                read = channel.read(byteBuffer);
            }

        } catch (IOException e) {
            System.out.println("IO Error, connection closed");
            channel.close();
            key.channel().close();
            key.cancel();
            return;
        }

        if (read == -1) {
            //connection terminated by client
            System.out.println("Client terminated connection");
            channel.close();
            key.channel().close();
            key.cancel();
            server.decrementConnectionCount();
            return;
        }
//TODO make hashing its own task once thread pool's implemented
        byte[] byteCopy = new byte[read];
        System.arraycopy(byteBuffer.array(), 0, byteCopy, 0, read);
        byte[] replyBytes = prepareReply(byteCopy);
        //                threadPoolManager.addTask(reply);
        server.incrementMessagesReceived();
        byteBuffer.clear();
        readyMessages.put(key, replyBytes);
        keyActions.get(key).remove(Character.valueOf('R'));
        key.interestOps(SelectionKey.OP_WRITE); //server won't write without this line?
    }

    public byte[] prepareReply(byte[] messageFromClient) {
        System.out.println(ComputeHash.SHA1FromBytes(messageFromClient));
        return ComputeHash.SHA1FromBytes(messageFromClient).getBytes();
    }
}