package cs455.scaling.client;

import cs455.scaling.tasks.ClientRead;
import cs455.scaling.tasks.ClientWrite;
import cs455.scaling.tasks.ComputeHash;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Client {

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
    private List<Character> clientCharList = new ArrayList<>();
    private Map<SelectionKey, List<Character>> clientPendingActions = new HashMap<>();

    private void startClient() throws IOException {
        clientChannel = SocketChannel.open();
        clientSelector = Selector.open();
        clientChannel.configureBlocking(false);
        clientChannel.connect(new InetSocketAddress(serverHost, serverPort));
        key = clientChannel.register(clientSelector, SelectionKey.OP_CONNECT);
    }

    private void startActions() throws IOException {
        int cycles = 0;
        while (cycles < 10) {
            clientSelector.select();
            Iterator keys = clientSelector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = (SelectionKey) keys.next();
                clientPendingActions.put(key, clientCharList);
                keys.remove();
                if (key.isConnectable()) {
                    connect(key);
                } else if (key.isReadable()) {
                    if (clientPendingActions.get(key).contains('R')) {

                    } else {
                        clientPendingActions.get(key).add('R');
                        read(key);
                    }
                } else if (key.isWritable()) {
                    if (clientPendingActions.get(key).contains('W')) {
                        //do nothing, action is already registered
                    } else {
                        clientPendingActions.get(key).add('W');
                        byte[] arrayToSend = prepareMessage();
                        computeAndStoreHash(arrayToSend);
                        write(key, arrayToSend);
                        cycles++;
                    }
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
        ClientRead clientRead = new ClientRead(key, bufferSize, this);
        clientRead.perform();
    }

    private void write(SelectionKey key, byte[] data) throws IOException {
        System.out.println("client serverWrite test");
        ClientWrite clientWrite = new ClientWrite(key, data, this);
        clientWrite.perform();
    }

    private byte[] prepareMessage() {
        byte[] byteArray = new byte[8000];
        for (int i = 0; i < byteArray.length - 1; i++) {
            byteArray[i] = (byte) ThreadLocalRandom.current().nextInt(127);
        }
        return byteArray;
    }

    private void computeAndStoreHash(byte[] bytes) {
        String newHash = ComputeHash.SHA1FromBytes(bytes);
        System.out.println(newHash);
        sentHashList.add(newHash);
    }

    public void incrementMessagesSent() {
        messagesSent++;
    }

    public void incrementMessagesReceived() {
        messagesReceived++;
    }

    public void checkForHashInList(String replyHash) {
        if (sentHashList.contains(replyHash)) {
            sentHashList.remove(replyHash);
            System.out.println("great success!");
        } else {
            System.out.println(replyHash);
            System.out.println("Message corrupted");
        }
    }

    public Map<SelectionKey, List<Character>> getPendingActions() { return clientPendingActions; }

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        serverHost = args[0];
        serverPort = Integer.parseInt(args[1]);
        messageRate = Integer.parseInt(args[2]);
        client.startClient();
        client.startActions();
    }
}
