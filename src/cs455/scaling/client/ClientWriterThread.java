package cs455.scaling.client;

import cs455.scaling.hash.ComputeHash;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Random;

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

    public void run() {
        while (true) {
            try {
                writeMessage();
                Thread.sleep(1000/messageRate);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Server disconnected, exiting...");
                break;
            }
        }
    }

    private void writeMessage() throws IOException {
        byte[] arrayToSend = prepareMessage();
        computeAndStoreHash(arrayToSend);
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = ByteBuffer.wrap(arrayToSend);
        channel.write(byteBuffer);
        client.incrementMessagesSent();
    }

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
