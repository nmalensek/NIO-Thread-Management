package cs455.scaling.tasks;

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
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        channel.write(byteBuffer);
        server.incrementMessagesSent();
        server.getPendingActions().get(key).remove(Character.valueOf('W'));
        key.interestOps(SelectionKey.OP_READ);
    }
}
