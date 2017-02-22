package cs455.scaling.tasks;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class AcceptConnection implements Task {

    private SelectionKey key;
    private Selector selector;

    public void setFields(SelectionKey key, Selector selector) {
        this.key = key;
        this.selector = selector;
    }

    public void perform() throws IOException, NullPointerException {
            ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
            SocketChannel channel = serverSocket.accept();

            System.out.println("Accepting incoming connection");
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }
}
