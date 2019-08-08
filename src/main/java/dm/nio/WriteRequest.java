package dm.nio;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class WriteRequest {
    private final ByteBuffer buf;
    private final SocketChannel ch;

    public WriteRequest(SocketChannel ch, ByteBuffer buf) {
        this.ch = ch;
        this.buf = buf;
    }

    public SocketChannel channel() {
        return ch;
    }

    public ByteBuffer buffer() {
        return buf;
    }
}
