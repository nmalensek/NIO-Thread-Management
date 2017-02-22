package cs455.scaling.tasks;

import cs455.scaling.Node;
import cs455.scaling.server.Server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Read implements Task {

    private SelectionKey key;
    private int bufferSize;
    private Node node;

    public Read(SelectionKey key, int bufferSize, Node node) {
        this.key = key;
        this.bufferSize = bufferSize;
        this.node = node;
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
            node.decrementConnectionCount();
            return;
        }

        key.interestOps(SelectionKey.OP_WRITE);
    }
}