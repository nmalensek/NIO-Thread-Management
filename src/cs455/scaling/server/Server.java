package cs455.scaling.server;

import cs455.scaling.tasks.AcceptConnection;
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
import java.util.Iterator;

public class Server {

    private static int portNum;
    private static int poolSize;
    private ThreadPoolManager threadPoolManager = ThreadPoolManager.getInstance();
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private int bufferSize = 8192; //sending 8kb messages
    private byte[] bytes = ByteBuffer.allocate(8000).putInt(555555555).array(); //test array
    AcceptConnection acceptConnection = new AcceptConnection();

    public void startServer() throws IOException {
        openChannels();
        threadPoolManager.addThreadsToPool(poolSize);
        listenForTasks();
    }

    private void listenForTasks() throws IOException {
        while (true) {
            try{
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
                        this.write(key, bytes);
                    }
                }
            } catch (NullPointerException npe) {
                System.out.println("null");
                break;
            }
        }
    }

    private synchronized void accept(SelectionKey key) throws IOException {
//        acceptConnection.setFields(key, selector);
//        threadPoolManager.addTask(acceptConnection);
        ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverSocket.accept();

        System.out.println("Accepting incoming connection");
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    private void read(SelectionKey key) throws IOException {
        Read read = new Read(key, bufferSize, this);
        threadPoolManager.addTask(read);
//        SocketChannel channel = (SocketChannel) key.channel();
//        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
//
//        int read = 0;
//
//        try {
//            while (byteBuffer.hasRemaining() && read != -1) {
//                read = channel.read(byteBuffer);
//            }
//        } catch (IOException e) {
//            System.out.println("IO Error, connection closed");
//            channel.close();
//            server.disconnect(key);
//            key.channel().close();
//            key.cancel();
//            return;
//        }
//
//        if (read == -1) {
//            //connection terminated by client
//            server.disconnect(key);
//            key.channel().close();
//            key.cancel();
//            return;
//        }
//
//        key.interestOps(SelectionKey.OP_WRITE);
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

    public void disconnect(SelectionKey key) throws IOException {
        key.channel().close();
        key.cancel();
        System.out.println("Connection closed");
    }

    private void openChannels() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(portNum));
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        portNum = Integer.parseInt(args[0]);
        poolSize = Integer.parseInt(args[1]);
        server.startServer();
    }
}
