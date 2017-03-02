package cs455.scaling.tasks.server;

import cs455.scaling.server.KeyBuffers;
import cs455.scaling.server.Server;
import cs455.scaling.tasks.common.Task;
import cs455.scaling.threadpool.ThreadPoolManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ServerWrite implements Task {

    private SelectionKey key;
    private byte[] data;
    private Server server;

    public ServerWrite(SelectionKey key, byte[] data, Server server) {
        this.key = key;
        this.data = data;
        this.server = server;
    }

    public synchronized void perform() throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            KeyBuffers keyBuffers = (KeyBuffers) key.attachment();
            ByteBuffer byteBuffer = keyBuffers.getWriteBuffer().wrap(data);
            byteBuffer.rewind();
            if (channel.write(byteBuffer) < data.length) {
                key.interestOps(SelectionKey.OP_WRITE);
                ServerWrite writeCopy = new ServerWrite(key, data, server);
                ThreadPoolManager.getInstance().addTask(writeCopy); //buffer full, write messages and copy write to TPM
            } else {
                System.out.println("Writing...");
                server.incrementMessagesSent();
                key.interestOps(SelectionKey.OP_READ);
                server.getPendingActions().get(key).remove(Character.valueOf('W'));
            }
        } catch (NullPointerException npe) {
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