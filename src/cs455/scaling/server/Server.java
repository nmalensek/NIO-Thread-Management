package cs455.scaling.server;

import cs455.scaling.tasks.server.ServerRead;
import cs455.scaling.tasks.server.ServerWrite;
import cs455.scaling.threadpool.ThreadPoolManager;
import cs455.scaling.tracking.ServerMessageTracker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;


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

    public void startServer() throws IOException {
        openChannels();
        threadPoolManager.addThreadsToPool(poolSize);
        serverMessageTracker.start();
        threadPoolManager.start();
        System.out.println("Listening on port " + portNum + "...");
        listenForTasks();
    }

    private void openChannels() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(portNum));
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

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

    private synchronized void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverSocket.accept();

        System.out.println("Accepting incoming connection");
        channel.configureBlocking(false);
        KeyBuffers keyBuffers = new KeyBuffers(bufferSize);
        channel.register(selector, SelectionKey.OP_READ, keyBuffers);
        incrementConnectionCount();
    }

    private synchronized void read(SelectionKey key) throws IOException {
        System.out.println("Reading...");
        ServerRead serverRead = new ServerRead(key, this, pendingMessages, pendingKeyActions);
        threadPoolManager.addTask(serverRead);
    }

    private void write(SelectionKey key, byte[] data) throws IOException {
        ServerWrite write = new ServerWrite(key, data, this);
        threadPoolManager.addTask(write);
    }

    private synchronized void checkRead(SelectionKey key) throws IOException {
        if (pendingKeyActions.get(key).contains('R')) {
            //do nothing, action is already registered
        } else {
            pendingKeyActions.get(key).add('R');
            read(key);
        }
    }

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