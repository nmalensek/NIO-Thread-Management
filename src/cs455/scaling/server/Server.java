package cs455.scaling.server;

import cs455.scaling.tasks.ServerRead;
import cs455.scaling.tasks.ServerWrite;
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
        ServerRead serverRead = new ServerRead(key, bufferSize, this, pendingMessages, pendingKeyActions);
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

    public synchronized void copyAndResetTrackers() {
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

/**
 * non-thread pool read method
 */
//        SocketChannel channel = (SocketChannel) key.channel();
//        KeyBuffers keyBuffers = (KeyBuffers) key.attachment();
//        ByteBuffer byteBuffer = keyBuffers.getReadBuffer();
//
//        int read = 0;
//
//        try {
//            while (byteBuffer.hasRemaining() && read != -1) {
//                read = channel.read(byteBuffer);
////                threadPoolManager.addTask(reply);
//            }
//
//        } catch (IOException e) {
//            System.out.println("IO Error, connection closed");
//            channel.close();
//            key.channel().close();
//            key.cancel();
//            return;
//        }
//
//        if (read == -1) {
//            //connection terminated by client
//            System.out.println("Client terminated connection");
//            channel.close();
//            key.channel().close();
//            key.cancel();
//            decrementConnectionCount();
//            return;
//        }
////TODO reimplement commented out code (makes hashing its own task) once thread pool's implemented
//        byte[] byteCopy = new byte[read];
//        System.arraycopy(byteBuffer.array(), 0, byteCopy, 0, read);
//        byte[] replyBytes = prepareReply(byteCopy);
//        incrementMessagesReceived();
//        byteBuffer.clear();
//        pendingMessages.put(key, replyBytes);
//        pendingKeyActions.get(key).remove(Character.valueOf('R'));
////        HashMessage hashMessage = new HashMessage(byteCopy, pendingMessages, pendingKeyActions, key);
////        threadPoolManager.addTask(hashMessage);
////        incrementMessagesReceived();
////        byteBuffer.clear();
//
//        key.interestOps(SelectionKey.OP_WRITE);

/**
 * non-thread pool write method
 */
//        try {
//            SocketChannel channel = (SocketChannel) key.channel();
//            KeyBuffers keyBuffers = (KeyBuffers) key.attachment();
//            ByteBuffer byteBuffer = keyBuffers.getWriteBuffer().wrap(data);
//            channel.write(byteBuffer);
//            incrementMessagesSent();
//            byteBuffer.clear();
//            key.interestOps(SelectionKey.OP_READ);
//            pendingKeyActions.get(key).remove(Character.valueOf('W'));
//        } catch (NullPointerException npe) {
//            System.out.println("There was no data to write");
//        }