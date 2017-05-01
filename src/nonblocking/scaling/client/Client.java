package nonblocking.scaling.client;

import nonblocking.scaling.tasks.client.ClientRead;
import nonblocking.scaling.tracking.ClientMessageTracker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

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

    /**
     * Starts up all components needed to connect to the server and begin sending messages.
     * @throws IOException
     */

    private void startClient() throws IOException {
        clientChannel = SocketChannel.open();
        clientSelector = Selector.open();
        clientChannel.configureBlocking(false);
        clientChannel.connect(new InetSocketAddress(serverHost, serverPort));
        key = clientChannel.register(clientSelector, SelectionKey.OP_CONNECT);
        startWriterThread();
        clientMessageTracker.start();
    }

    /**
     * Starts thread that prints client's current message output.
     */

    private void startWriterThread() {
        ClientWriterThread clientWriterThread = new ClientWriterThread(this, key, messageRate, sentHashList);
        clientWriterThread.start();
    }

    /**
     * Method code adapted from code given by instructor during lab help session:
     * http://www.cs.colostate.edu/~cs455/lectures/CS455-HelpSession5.pdf
     * Iterators through SelectionKeys and performs the appropriate action. Connect action is done when making
     * contact with the server; afterward the key remains Readable as long as the program's running.
     * @throws IOException
     */

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

    /**
     * Connects to the server specified in the command line arguments. Sets interest to read, allowing
     * client to read messages received from the server. This is the only place the read interest is set.
     * @param key Client's SelectionKey that needs to be registered with the server.
     * @throws IOException
     */

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

    /**
     * Sets number of messages sent and received in the ClientMessageTracker class, then resets counters
     * to 0. This provides the client's throughput for the duration specified in ClientMessageTracker's run() method.
     */

    public void copyAndResetTrackers() {
        clientMessageTracker.setCurrentSentMessages(messagesSent);
        clientMessageTracker.setCurrentReceivedMessages(messagesReceived);
        messagesSent = 0;
        messagesReceived = 0;
    }

    /**
     * Checks if the hash code returned by the server matches a hash code the client has previously
     * sent. If the received hash code matches a hash code the client sent, the hash code is removed
     * and the number of messages received is incremented by one. Otherwise, the message is discarded.
     * @param replyHash Hash code received from the server.
     */

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

    public void shutdownMessageTracker() {
        clientMessageTracker.shutdown();
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
