package cs455.scaling.tasks;

import cs455.scaling.Node;
import cs455.scaling.client.Client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ClientRead implements Task {
    private SelectionKey key;
    private int bufferSize;
    private Client client;

    public ClientRead(SelectionKey key, int bufferSize, Client client) {
        this.key = key;
        this.bufferSize = bufferSize;
        this.client = client;
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
            //connection terminated by server
            System.out.println("Server terminated connection");
            channel.close();
            key.channel().close();
            key.cancel();
            client.decrementConnectionCount();
            return;
        }

        byte[] byteCopy = new byte[read];
        System.arraycopy(byteBuffer.array(), 0, byteCopy, 0, read);
        String packetContents = new String(byteCopy);
        client.checkForHashInList(packetContents);
        key.interestOps(SelectionKey.OP_WRITE);
    }
}
