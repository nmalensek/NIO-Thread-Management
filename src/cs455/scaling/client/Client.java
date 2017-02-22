package cs455.scaling.client;

import cs455.scaling.Node;
import cs455.scaling.tasks.ComputeHash;
import cs455.scaling.tasks.Read;
import cs455.scaling.tasks.Write;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

public class Client implements Node {

    private static String serverHost;
    private static int serverPort;
    private static int messageRate;
    private SocketChannel clientChannel;
    private Selector clientSelector;
    private SelectionKey key;
    private int bufferSize = 8000;
    private LinkedList<String> sentHashList = new LinkedList<>();
    private int messagesSent;
    private int messagesReceived;

    private void startClient() throws IOException {
        clientChannel = SocketChannel.open();
        clientSelector = Selector.open();
        clientChannel.configureBlocking(false);
        clientChannel.connect(new InetSocketAddress(serverHost, serverPort));
        key = clientChannel.register(clientSelector, SelectionKey.OP_CONNECT);
    }

    private void startActions() throws IOException {
        while (true) {
            clientSelector.select();
            Iterator keys = this.clientSelector.selectedKeys().iterator();
            while(keys.hasNext()) {
                SelectionKey key = (SelectionKey) keys.next();
                keys.remove();
                if (key.isConnectable()) {
                    this.connect(key);
                } else if (key.isReadable()) {
                    this.read(key);
                } else if (key.isWritable()) {
                    byte[] arrayToSend = prepareMessage();
                    computeAndStoreHash(arrayToSend);
                    this.write(key, arrayToSend);
                }
            }
        }
    }

    private void connect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.finishConnect();
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private void read(SelectionKey key) throws IOException {
        System.out.println("client read test");
        Read read = new Read(key, bufferSize, this);
        read.perform();
    }

    private void write(SelectionKey key, byte[] data) throws IOException {
        System.out.println("client write test");
        Write write = new Write(key, data);
        write.perform();
    }

    private byte[] prepareMessage() {
        byte[] byteArray = new byte[8000];
        for (int i = 0; i < byteArray.length-1; i++) {
            byteArray[i] = (byte) ThreadLocalRandom.current().nextInt(127);
        }
        return byteArray;
    }

    private void computeAndStoreHash(byte[] bytes) {
        String newHash = ComputeHash.SHA1FromBytes(bytes);
        sentHashList.add(newHash);
    }

    public void incrementMessagesSent() {
        messagesSent++;
    }

    public void incrementMessagesReceived() {
        messagesReceived++;
    }
    public void incrementConnectionCount() {
        //unused
    }
    public void decrementConnectionCount() {
        //unused
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        serverHost = args[0];
        serverPort = Integer.parseInt(args[1]);
        messageRate = Integer.parseInt(args[2]);
        client.startClient();
        client.startActions();
    }
}
