package cs455.scaling.server;

import java.nio.ByteBuffer;

public class KeyBuffers {

    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;

    public KeyBuffers(int bufferSize) {
        readBuffer = ByteBuffer.allocate(bufferSize);
        writeBuffer = ByteBuffer.allocate(bufferSize);
    }

    public ByteBuffer getReadBuffer() {
        return readBuffer;
    }

    public ByteBuffer getWriteBuffer() {
        return writeBuffer;
    }
}
