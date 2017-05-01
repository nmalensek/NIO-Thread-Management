package nonblocking.scaling.tasks.server;

import nonblocking.scaling.server.KeyBuffers;
import nonblocking.scaling.server.Server;
import nonblocking.scaling.tasks.common.Task;
import nonblocking.scaling.threadpool.ThreadPoolManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Code adapted from code given by instructor during lab help session:
 * http://www.cs.colostate.edu/~cs455/lectures/CS455-HelpSession5.pdf
 */

public class ServerWrite implements Task {

    private SelectionKey key;
    private byte[] data;
    private Server server;

    public ServerWrite(SelectionKey key, byte[] data, Server server) {
        this.key = key;
        this.data = data;
        this.server = server;
    }

    /**
     * Writes response to client in the client's SocketChannel. The messages are only sent by
     * setting OP_WRITE on the SelectionKey when the underlying ByteBuffer is full
     * (an entire message can't be written to the channel). If the buffer's full, the message
     * that was attempted to be written to the SocketChannel is copied and put back into the
     * task queue so it can be written successfully next time.
     * @throws IOException
     */

    public synchronized void perform() throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            KeyBuffers keyBuffers = (KeyBuffers) key.attachment();
            ByteBuffer byteBuffer = keyBuffers.getWriteBuffer().wrap(data);
            byteBuffer.rewind();
            if (channel.write(byteBuffer) < data.length) {
                key.interestOps(SelectionKey.OP_WRITE);
                ServerWrite writeCopy = new ServerWrite(key, data, server);
                ThreadPoolManager.getInstance().addTask(writeCopy);
            } else {
//                System.out.println("Writing...");
                server.incrementMessagesSent();
                key.interestOps(SelectionKey.OP_READ);
                server.getPendingActions().get(key).remove(Character.valueOf('W'));
            }
        } catch (NullPointerException npe) {
            //shouldn't happen anymore because OP.WRITE is only set when the underlying ByteBuffer is full.
            System.out.println("Nothing to write");
            key.interestOps(SelectionKey.OP_READ);
            server.getPendingActions().get(key).remove(Character.valueOf('W'));
        } catch (IOException e) {
            System.out.println("IO Error, connection closed");
            channel.close();
            key.channel().close();
            key.cancel();
            return;
        }
    }
}