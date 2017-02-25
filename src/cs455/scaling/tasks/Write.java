package cs455.scaling.tasks;

import cs455.scaling.Node;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Write implements Task {

    private SelectionKey key;
    private byte[] data;
    private Node node;

    public Write(SelectionKey key, byte[] data, Node node) {
        this.key = key;
        this.data = data;
        this.node = node;
    }

    public synchronized void perform() throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        channel.write(byteBuffer);
        node.incrementMessagesSent();
        key.interestOps(SelectionKey.OP_READ);
    }
}
