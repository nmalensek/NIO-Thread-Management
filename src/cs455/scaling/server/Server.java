package cs455.scaling.server;

import cs455.scaling.Node;
import cs455.scaling.tasks.Read;
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
import java.util.Iterator;

public class Server implements Node {

    private static int portNum;
    private static int poolSize;
    private ThreadPoolManager threadPoolManager = ThreadPoolManager.getInstance();
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private int bufferSize = 8000; //sending 8kb messages
    private byte[] bytes = ByteBuffer.allocate(8000).putInt(555555555).array(); //test array
    private int activeConnections;
    private int sentMessages;
    private int receivedMessages;

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
                if (key.isAcceptable() && key.isValid()) {
                    this.accept(key);
                    System.out.println(activeConnections);
                } else if (key.isReadable() && key.isValid()) {
                    this.read(key);
                } else if (key.isWritable() && key.isValid()) {
                    this.write(key, bytes);
                }
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverSocket.accept();

        System.out.println("Accepting incoming connection");
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        incrementConnectionCount();
    }

    private void read(SelectionKey key) throws IOException {
        Read read = new Read(key, bufferSize, this);
        threadPoolManager.addTask(read);
    }

    private void write(SelectionKey key, byte[] data) throws IOException {
        Write write = new Write(key, data);
        write.perform();
//        SocketChannel channel = (SocketChannel) key.channel();
//        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
//        channel.write(byteBuffer);
//        System.out.println("test");
//        key.interestOps(SelectionKey.OP_READ);
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

    public int calculateThroughput() {
        int currentSentMessages = sentMessages;
        int currentReceivedMessages = receivedMessages;
        int throughput = (currentSentMessages + currentReceivedMessages)/5;
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
