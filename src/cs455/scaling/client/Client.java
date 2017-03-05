package cs455.scaling.client;

import cs455.scaling.tasks.client.ClientRead;
import cs455.scaling.tracking.ClientMessageTracker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;


public class Client {

    private static String serverHost;
    private static int serverPort;
    private static int messageRate;
    private SocketChannel clientChannel;
    private Selector clientSelector;
    private SelectionKey key;
    private int bufferSize = 8000;
    private List<String> sentHashList =
            Collections.synchronizedList(new LinkedList<>());
    private int messagesSent;
    private int messagesReceived;
    private ClientMessageTracker clientMessageTracker = new ClientMessageTracker(this);
    private List<Character> clientCharList = new ArrayList<>();
    private Map<SelectionKey, List<Character>> clientPendingActions = new HashMap<>();

    private void startClient() throws IOException {
        clientChannel = SocketChannel.open();
        clientSelector = Selector.open();
        clientChannel.configureBlocking(false);
        clientChannel.connect(new InetSocketAddress(serverHost, serverPort));
        key = clientChannel.register(clientSelector, SelectionKey.OP_CONNECT);
        startWriterThread();
        clientMessageTracker.start();
    }

    private void startWriterThread() {
        ClientWriterThread clientWriterThread = new ClientWriterThread(this, key, messageRate, sentHashList);
        clientWriterThread.start();
    }

    private void startActions() throws IOException {
        while (true) {
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
                        //do nothing, key is already registered for a read
                    } else {
                        clientPendingActions.get(key).add('R');
                        read(key);
                    }
                }
            }
        }
    }


    private void connect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.finishConnect();
        key.interestOps(SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        ClientRead clientRead = new ClientRead(key, bufferSize, this);
        clientRead.perform();
    }

    public synchronized void incrementMessagesSent() { messagesSent++; }
    public synchronized void incrementMessagesReceived() {
        messagesReceived++;
    }

    public void copyAndResetTrackers() {
        clientMessageTracker.setCurrentSentMessages(messagesSent);
        clientMessageTracker.setCurrentReceivedMessages(messagesReceived);
        messagesSent = 0;
        messagesReceived = 0;
    }

    public void checkForHashInList(String replyHash) {
        if (sentHashList.contains(replyHash)) {
            sentHashList.remove(replyHash);
            incrementMessagesReceived();
        } else {

        }
    }

    public Map<SelectionKey, List<Character>> getPendingActions() {
        return clientPendingActions;
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
