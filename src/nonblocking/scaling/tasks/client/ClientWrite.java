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
 *
 * Unused, replaced by ClientWriterThread class.
 */

public class ClientWrite implements Task {
    private SelectionKey key;
    private byte[] data;
    private Client client;

    public ClientWrite(SelectionKey key, byte[] data, Client client) {
        this.key = key;
        this.data = data;
        this.client = client;
    }

    public synchronized void perform() throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        channel.write(byteBuffer);
        client.incrementMessagesSent();
//        client.getPendingActions().get(key).remove(Character.valueOf('W'));
//        key.interestOps(SelectionKey.OP_READ);
    }
}

