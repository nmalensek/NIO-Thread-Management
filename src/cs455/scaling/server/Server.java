package cs455.scaling.server;

import cs455.scaling.tasks.ComputeHash;
import cs455.scaling.threadpool.ThreadPoolManager;
import cs455.scaling.tracking.ServerMessageTracker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
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
                    if (pendingKeyActions.get(key).contains('R')) {
                        //do nothing, action is already registered
                    } else {
                        pendingKeyActions.get(key).add('R');
                        read(key);
                    }
                } else if (key.isWritable()) {
                    if (pendingKeyActions.get(key).contains('W')) {
                        //do nothing, action is already registered
                    } else {
                        pendingKeyActions.get(key).add('W');
                        write(key, pendingMessages.get(key));
                    }
                }
            }
        }
    }

    private synchronized void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverSocket.accept();

        System.out.println("Accepting incoming connection");
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        incrementConnectionCount();
    }

    private synchronized void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);

        int read = 0;

        try {
            while (byteBuffer.hasRemaining() && read != -1) {
                read = channel.read(byteBuffer);
//                threadPoolManager.addTask(reply);
            }

        } catch (IOException e) {
            System.out.println("IO Error, connection closed");
            channel.close();
            key.channel().close();
            key.cancel();
            return;
        }

        if (read == -1) {
            //connection terminated by client
            System.out.println("Client terminated connection");
            channel.close();
            key.channel().close();
            key.cancel();
            decrementConnectionCount();
            return;
        }

        byte[] byteCopy = new byte[read];
        System.arraycopy(byteBuffer.array(), 0, byteCopy, 0, read);
        byte[] replyBytes = prepareReply(byteCopy);
        incrementMessagesReceived();
        byteBuffer.clear();
        pendingMessages.put(key, replyBytes);
        pendingKeyActions.get(key).remove(Character.valueOf('R'));

        key.interestOps(SelectionKey.OP_WRITE);
//        ServerRead serverRead = new ServerRead(key, bufferSize, this);
//        threadPoolManager.addTask(serverRead);
    }

    private void write(SelectionKey key, byte[] data) throws IOException {
        try {
            SocketChannel channel = (SocketChannel) key.channel();
            ByteBuffer byteBuffer = ByteBuffer.wrap(data);
            channel.write(byteBuffer);
            incrementMessagesSent();
            key.interestOps(SelectionKey.OP_READ);
            pendingKeyActions.get(key).remove(Character.valueOf('W'));
        } catch (NullPointerException npe) {
            System.out.println("There was no data to write");
        }
//        System.out.println("server write test");
//        ServerWrite write = new ServerWrite(key, data, this);
//        threadPoolManager.addTask(write);
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

    public Map<SelectionKey, List<Character>> getPendingActions() { return pendingKeyActions; }

    public void copyTrackers() {
        serverMessageTracker.setCurrentSentMessages(sentMessages);
        serverMessageTracker.setCurrentReceivedMessages(receivedMessages);
        serverMessageTracker.setCurrentActiveConnections(activeConnections);
    }

    private byte[] prepareReply(byte[] messageFromClient) {
        System.out.println(ComputeHash.SHA1FromBytes(messageFromClient));
        return ComputeHash.SHA1FromBytes(messageFromClient).getBytes();
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        portNum = Integer.parseInt(args[0]);
        poolSize = Integer.parseInt(args[1]);
        server.startServer();
    }
}
