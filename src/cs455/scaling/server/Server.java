package cs455.scaling.server;

import cs455.scaling.threadpool.ThreadPoolManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

public class Server {

    private static int portNum;
    private static int poolSize;
    private ThreadPoolManager threadPoolManager = ThreadPoolManager.getInstance();
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    public void startServer() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(portNum));
        selector = Selector.open();
        threadPoolManager.addThreadsToPool(poolSize);
        while (true) {
            this.selector.select();
            Iterator keys = this.selector.selectedKeys().iterator();
            while (keys.hasNext()) {

            }
        }
    }

    public static void main(String[] args) throws IOException {
        portNum = Integer.parseInt(args[0]);
        poolSize = Integer.parseInt(args[1]);
        Server server = new Server();
        server.startServer();
    }
}
