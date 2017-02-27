package cs455.scaling.tasks;

import cs455.scaling.server.KeyBuffers;
import cs455.scaling.server.Server;

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
            channel.write(byteBuffer);
            server.incrementMessagesSent();
            byteBuffer.clear();
            server.getPendingActions().get(key).remove(Character.valueOf('W'));
            key.interestOps(SelectionKey.OP_READ);
        } catch (NullPointerException npe) {
            System.out.println("There was no data to write");
        } catch (IOException e) {
            System.out.println("IO Error, connection closed");
            channel.close();
            key.channel().close();
            key.cancel();
            return;
        }
    }
}