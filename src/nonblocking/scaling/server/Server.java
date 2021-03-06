package nonblocking.scaling.server;

import nonblocking.scaling.tasks.server.ServerRead;
import nonblocking.scaling.tasks.server.ServerWrite;
import nonblocking.scaling.threadpool.ThreadPoolManager;
import nonblocking.scaling.tracking.ServerMessageTracker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;


public class Server {

    private static int portNum;
    private static int poolSize;
    private ThreadPoolManager threadPoolManager = ThreadPoolManager.getInstance();
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private int bufferSize = 8000; //sending 8kb messages
    private int activeConnections;
    private int sentMessages;
    private int receivedMessages;
    private ServerMessageTracker serverMessageTracker = new ServerMessageTracker(this);
    private List<Character> charList =
            Collections.synchronizedList(new ArrayList<>());
    private Map<SelectionKey, byte[]> pendingMessages =
            Collections.synchronizedMap(new HashMap<>());
    private Map<SelectionKey, List<Character>> pendingKeyActions =
            Collections.synchronizedMap(new HashMap<>());

    /**
     * Starts server, creates thread pool, starts necessary threads, then listens for incoming
     * connections and tasks.
     * @throws IOException
     */

    public void startServer() throws IOException {
        openChannels();
        threadPoolManager.addThreadsToPool(poolSize);
        serverMessageTracker.start();
        threadPoolManager.start();
        System.out.println("Listening on port " + portNum + "...");
        listenForTasks();
    }

    /**
     * Allows new clients to connect to the server by opening the ServerSocketChannel
     * and setting the intention to accepting connections.
     * @throws IOException
     */

    private void openChannels() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(portNum));
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    /**
     * Method code adapted from code given by instructor during lab help session:
     * http://www.cs.colostate.edu/~cs455/lectures/CS455-HelpSession5.pdf
     * Iterates through server's registered client keys and performs the appropriate action
     * depending on the key's intent.
     * @throws IOException
     */

    private void listenForTasks() throws IOException {
        while (true) {
            selector.select();
            Iterator keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = (SelectionKey) keys.next();
                pendingKeyActions.put(key, charList);
                keys.remove();
                if (key.isAcceptable()) {
                    accept(key);
                } else if (key.isReadable()) {
                    checkRead(key);
                } else if (key.isWritable()) {
                    checkWrite(key);
                }
            }
        }
    }

    /**
     * Method code adapted from code given by instructor during lab help session:
     * http://www.cs.colostate.edu/~cs455/lectures/CS455-HelpSession5.pdf.
     * Accepts incoming client connections. Must be performed by the server itself
     * and not by a worker thread for the connection to successfully complete.
     * Also attaches read and write ByteBuffers to the key to carry out those actions.
     * @param key New client's SelectionKey.
     * @throws IOException
     */

    private synchronized void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverSocket.accept();

        System.out.println("Accepting incoming connection");
        channel.configureBlocking(false);
        KeyBuffers keyBuffers = new KeyBuffers(bufferSize);
        channel.register(selector, SelectionKey.OP_READ, keyBuffers);
        incrementConnectionCount();
    }

    /**
     * Creates a new ServerRead task for a worker thread to execute.
     * @param key Sender's SelectionKey.
     * @throws IOException
     */

    private synchronized void read(SelectionKey key) throws IOException {
//        System.out.println("Reading...");
        ServerRead serverRead = new ServerRead(key, this, pendingMessages, pendingKeyActions);
        threadPoolManager.addTask(serverRead);
    }

    /**
     * Creates a new ServerWrite task for a worker thread to execute.
     * @param key Sender's SelectionKey.
     * @throws IOException
     */

    private void write(SelectionKey key, byte[] data) throws IOException {
        ServerWrite write = new ServerWrite(key, data, this);
        threadPoolManager.addTask(write);
    }

    /**
     * Checks if the key has already been registered as needing a read action.
     * If it has been, the key Iterator in listenForTasks continues. Otherwise,
     * the server logs that the key wants to be read. This is to prevent a key
     * from creating multiple read tasks for the data it sent.
     * @param key Sender's SelectionKey.
     * @throws IOException
     */

    private synchronized void checkRead(SelectionKey key) throws IOException {
        if (pendingKeyActions.get(key).contains('R')) {
            //do nothing, action is already registered
        } else {
            pendingKeyActions.get(key).add('R');
            read(key);
        }
    }

    /**
     * Checks if the key has already been registered as needing a write action.
     * If it has been, the key Iterator in listenForTasks continues. Otherwise,
     * the server logs that the key wants to write. This is to prevent a key
     * from creating multiple write tasks that will result in faulty messages
     * (a null byte array is passed into the ServerWrite method).
     * @param key Message receiver's SelectionKey.
     * @throws IOException
     */

    private synchronized void checkWrite(SelectionKey key) throws IOException {
        if (pendingKeyActions.get(key).contains('W')) {
            //do nothing, action is already registered
        } else {
            pendingKeyActions.get(key).add('W');
            write(key, pendingMessages.remove(key));
        }
    }

    public synchronized void incrementMessagesSent() {
        sentMessages++;
    }

    public synchronized void incrementMessagesReceived() {
        receivedMessages++;
    }

    private void incrementConnectionCount() {
        activeConnections++;
    }

    public synchronized void decrementConnectionCount() {
        activeConnections--;
    }

    public Map<SelectionKey, List<Character>> getPendingActions() {
        return pendingKeyActions;
    }

    /**
     * Sets number of messages sent and received in the ServerMessageTracker class, then resets counters
     * to 0. This provides the client's throughput for the duration specified in ServerMessageTracker's run() method.
     */

    public void copyAndResetTrackers() {
        serverMessageTracker.setCurrentSentMessages(sentMessages);
        serverMessageTracker.setCurrentReceivedMessages(receivedMessages);
        serverMessageTracker.setCurrentActiveConnections(activeConnections);
        sentMessages = 0;
        receivedMessages = 0;
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        portNum = Integer.parseInt(args[0]);
        poolSize = Integer.parseInt(args[1]);
        server.startServer();
    }
}