package nonblocking.scaling.tasks.client;

import nonblocking.scaling.client.Client;
import nonblocking.scaling.tasks.common.Task;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Code adapted from code given by instructor during lab help session:
 * http://www.cs.colostate.edu/~cs455/lectures/CS455-HelpSession5.pdf
 */

public class ClientRead implements Task {
    private SelectionKey key;
    private int bufferSize;
    private Client client;

    public ClientRead(SelectionKey key, int bufferSize, Client client) {
        this.key = key;
        this.bufferSize = bufferSize;
        this.client = client;
    }

    /**
     * Reads incoming message from server. Method reads while the ByteBuffer has bytes to read remaining
     * in the SocketChannel and the connection is still active. If the SocketChannel has 0 bytes to read,
     * the ByteBuffer is empty and the message can be processed by checking the list of sent hash codes.
     * The "read" action is then marked as complete by removing the R character from the pending actions
     * list.
     * @throws IOException
     */

    public void perform() throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);

        int read = 0;

        try {
            while (byteBuffer.hasRemaining() && read != -1) {
                read = channel.read(byteBuffer);
                if (read > 0) {
                    byte[] byteCopy = new byte[read];
                    System.arraycopy(byteBuffer.array(), 0, byteCopy, 0, read);
                    String packetContents = new String(byteCopy);
                    client.checkForHashInList(packetContents);
                    client.getPendingActions().get(key).remove(Character.valueOf('R'));
                } else {
                    byteBuffer.position(byteBuffer.limit());
                }
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
            client.shutdownMessageTracker();
            return;
        }

//        key.interestOps(SelectionKey.OP_WRITE);
    }
}
