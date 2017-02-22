package cs455.scaling.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;

public class Client {

    private static String serverHost;
    private static int serverPort;
    private static int messageRate;
    private SocketChannel clientChannel;
    private Selector clientSelector;
    private SelectionKey key;
    private LinkedList<String> hashList = new LinkedList<>();

    private void startClient() throws IOException {
        clientChannel = clientChannel.open();
        clientSelector = clientSelector.open();
        clientChannel.configureBlocking(false);
        clientChannel.connect(new InetSocketAddress(serverHost, serverPort));
        key = clientChannel.register(clientSelector, SelectionKey.OP_CONNECT);
    }

    private void connect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.finishConnect();
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private void startActions() throws IOException {
        while (true) {
            clientSelector.select();
            Iterator keys = this.clientSelector.selectedKeys().iterator();
            while(keys.hasNext()) {
                SelectionKey key = (SelectionKey) keys.next();
                keys.remove();
                if (key.isConnectable()) {
                    this.connect(key);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        serverHost = args[0];
        serverPort = Integer.parseInt(args[1]);
        messageRate = Integer.parseInt(args[2]);
        client.startClient();
        client.startActions();
    }
}
