package cs455.scaling.client;

import cs455.scaling.hash.ComputeHash;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Random;

/**
 * Code adapted from code given by instructor during lab help session:
 * http://www.cs.colostate.edu/~cs455/lectures/CS455-HelpSession5.pdf
 */

public class ClientWriterThread extends Thread {

    private Client client;
    private int messageRate;
    private SelectionKey key;
    private List<String> hashList;

    public ClientWriterThread(Client client, SelectionKey key, int messageRate, List<String> hashList) {
        this.client = client;
        this.messageRate = messageRate;
        this.key = key;
        this.hashList = hashList;
    }

    /**
     * Writes messages per second at the rate specified by third command line argument for Client class
     */

    public void run() {
        while (true) {
            try {
                writeMessage();
                Thread.sleep(1000/messageRate);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    /**
     * Constructs message containing random byte array and writes it to the client's SocketChannel
     * @throws IOException
     */

    private void writeMessage() throws IOException {
        byte[] arrayToSend = prepareMessage();
        computeAndStoreHash(arrayToSend);
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = ByteBuffer.wrap(arrayToSend);
        channel.write(byteBuffer);
        client.incrementMessagesSent();
    }

    /**
     * Constructs random 8 kb byte array to send to the server
     * @return byte array of random bytes
     */

    private byte[] prepareMessage() {
        byte[] byteArray = new byte[8000];
        new Random().nextBytes(byteArray);
        return byteArray;
    }

    private void computeAndStoreHash(byte[] bytes) {
        String newHash = ComputeHash.SHA1FromBytes(bytes);
        hashList.add(newHash);
    }
}
