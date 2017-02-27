package cs455.scaling.tasks;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.List;
import java.util.Map;

public class HashMessage implements Task {

    private byte[] replyBytes;
    private Map<SelectionKey, byte[]> readyMessages;
    private Map<SelectionKey, List<Character>> keyActions;
    private SelectionKey key;

    public HashMessage(byte[] replyBytes, Map<SelectionKey, byte[]> readyMessages,
                       Map<SelectionKey, List<Character>> keyActions, SelectionKey key) {
        this.replyBytes = replyBytes;
        this.readyMessages = readyMessages;
        this.keyActions = keyActions;
        this.key = key;
    }

    public void perform() throws IOException {
        byte[] bytesToSend = prepareReply(replyBytes);
        readyMessages.put(key, bytesToSend);
        keyActions.get(key).remove(Character.valueOf('R'));
    }

    private byte[] prepareReply(byte[] messageFromClient) {
        System.out.println(ComputeHash.SHA1FromBytes(messageFromClient));
        return ComputeHash.SHA1FromBytes(messageFromClient).getBytes();
    }
}
