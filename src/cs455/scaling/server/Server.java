package cs455.scaling.server;

import cs455.scaling.missions.Mission;
import cs455.scaling.missions.Read;
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
    private int bufferSize = 8000; //sending 8kb messages

    public void startServer() throws IOException {
        openChannels();
        threadPoolManager.addThreadsToPool(poolSize);
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
                    this.write(key);
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
    }

    private void read(SelectionKey key) throws IOException {
        Read read = new Read(key, bufferSize, this);
        threadPoolManager.addTask(read);
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
//        ByteBuffer byteBuffer = ByteBuffer.wrap(data); //byte[]
//        channel.write(byteBuffer);
        key.interestOps(SelectionKey.OP_READ);
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
