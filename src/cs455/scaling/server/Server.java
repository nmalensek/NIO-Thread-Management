package cs455.scaling.server;

import cs455.scaling.Node;
import cs455.scaling.tasks.ComputeHash;
import cs455.scaling.tasks.ServerRead;
import cs455.scaling.tasks.Write;
import cs455.scaling.threadpool.ThreadPoolManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Server implements Node {

    private static int portNum;
    private static int poolSize;
    private ThreadPoolManager threadPoolManager = ThreadPoolManager.getInstance();
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private int bufferSize = 8000; //sending 8kb messages
    private int activeConnections;
    private int sentMessages;
    private int receivedMessages;
    private Map<SelectionKey, byte[]> pendingMessages = new HashMap<>();

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
            this.selector.select();
            Iterator keys = this.selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = (SelectionKey) keys.next();
                keys.remove();
                if (key.isAcceptable()) {
                    this.accept(key);
                } else if (key.isReadable()) {
                    this.read(key);
                } else if (key.isWritable()) {
                    this.write(key, pendingMessages.get(key));
                }
            }
        }
    }

    private synchronized void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverSocket.accept();

        System.out.println("Accepting incoming connection");
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        incrementConnectionCount();
    }

    private synchronized void read(SelectionKey key) throws IOException {
//        SocketChannel channel = (SocketChannel) key.channel();
//        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
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
//
//        byte[] byteCopy = new byte[read];
//        System.arraycopy(byteBuffer.array(), 0, byteCopy, 0, read);
//        byte[] replyBytes = prepareReply(byteCopy);
//        incrementMessagesReceived();
//        byteBuffer.clear();
//        pendingMessages.put(key, replyBytes);
//
//        key.interestOps(SelectionKey.OP_WRITE);
        ServerRead serverRead = new ServerRead(key, bufferSize, this);
        threadPoolManager.addTask(serverRead);
    }

    private void write(SelectionKey key, byte[] data) throws IOException {
//        try {
//            SocketChannel channel = (SocketChannel) key.channel();
//            ByteBuffer byteBuffer = ByteBuffer.wrap(data);
//            channel.write(byteBuffer);
//            incrementMessagesSent();
//            key.interestOps(SelectionKey.OP_READ);
//        } catch (NullPointerException npe) {
//            System.out.println("There was no data to write");
//        }
        System.out.println("server write test");
        Write write = new Write(key, data, this);
        threadPoolManager.addTask(write);
    }

    public synchronized void incrementMessagesSent() {
        sentMessages++;
    }

    public synchronized void incrementMessagesReceived() {
        receivedMessages++;
    }

    public void incrementConnectionCount() {
        activeConnections++;
    }

    public synchronized void decrementConnectionCount() {
        activeConnections--;
    }

    private byte[] prepareReply(byte[] messageFromClient) {
        return ComputeHash.SHA1FromBytes(messageFromClient).getBytes();
    }

    public int calculateThroughput() {
        int currentSentMessages = sentMessages;
        int currentReceivedMessages = receivedMessages;
        int throughput = (currentSentMessages + currentReceivedMessages) / 5;
        return throughput;
    }

    public void printThroughputMessage(int throughput) {
        String throughputMessage = "";
        throughputMessage += LocalDateTime.now().toString() + " ";
        throughputMessage += "Current Server Throughput: " + throughput + " messages/s ";
        throughputMessage += "Active Client Connections: " + activeConnections;
        System.out.println(throughputMessage);
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        portNum = Integer.parseInt(args[0]);
        poolSize = Integer.parseInt(args[1]);
        server.startServer();
    }
}
