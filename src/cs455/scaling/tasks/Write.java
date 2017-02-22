package cs455.scaling.tasks;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Write implements Task {

    private SelectionKey key;
    private byte[] data;

    public Write(SelectionKey key, byte[] data) {
        this.key = key;
        this.data = data;
    }

    public void perform() throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        channel.write(byteBuffer);
        System.out.println("test");
        key.interestOps(SelectionKey.OP_READ);
    }
}
