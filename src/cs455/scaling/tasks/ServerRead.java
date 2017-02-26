package cs455.scaling.tasks;

import cs455.scaling.server.Server;
import cs455.scaling.threadpool.ThreadPoolManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ServerRead implements Task {

    private SelectionKey key;
    private int bufferSize;
    private Server server;
    private ThreadPoolManager threadPoolManager = ThreadPoolManager.getInstance();

    public ServerRead(SelectionKey key, int bufferSize, Server server) {
        this.key = key;
        this.bufferSize = bufferSize;
        this.server = server;
    }

    public void perform() throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);

        int read = 0;

        try {
            while (byteBuffer.hasRemaining() && read != -1) {
                read = channel.read(byteBuffer);
                byte[] byteCopy = new byte[read];
                System.arraycopy(byteBuffer.array(), 0, byteCopy, 0, read);
                server.incrementMessagesReceived();
                ServerWrite reply = new ServerWrite(key, byteCopy, server);
                threadPoolManager.addTask(reply);
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

//        key.interestOps(SelectionKey.OP_WRITE);
    }
}