package cs455.scaling.missions;

import cs455.scaling.server.Server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Read implements Mission {

    private SelectionKey key;
    private int bufferSize;
    private Server server;

    public Read(SelectionKey key, int bufferSize, Server server) {
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
            }
        } catch (IOException e) {
            server.disconnect(key);
            return;
        }

        if (read == -1) {
            //connection terminated by client
            server.disconnect(key);
            return;
        }

        key.interestOps(SelectionKey.OP_WRITE);
    }
}
