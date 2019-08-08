package dm.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.BlockingQueue;

import static dm.nio.Properties.*;
import static java.nio.channels.SelectionKey.OP_READ;

public class NioReader extends Worker {
    private final BlockingQueue<SocketChannel> channels;
    private final Selector selector;

    public NioReader(int idx, BlockingQueue<SocketChannel> channels) throws IOException {
        super("receiver-" + idx);

        this.channels = channels;
        this.selector = SelectorProvider.provider().openSelector();
    }

    @Override
    public void run() {
        log.log("Starting a NIO reader thread.");

        try {
            while (!Thread.interrupted()) {
                SocketChannel ch = channels.poll();

                if (ch != null)
                    registerChannel(ch);

                selector.select(2000);

                for (SelectionKey key : selector.selectedKeys()) {
                    if (key.isReadable())
                        processRead(key);
                    else if (key.isWritable())
                        processWrite(key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerChannel(SocketChannel ch) throws IOException {
        log.log("Registering a new channel: " + ch);

        SelectionKey key = ch.register(selector, SelectionKey.OP_WRITE);
        key.attach(new ReadingState());
    }

    private void processRead(SelectionKey key) throws IOException {
        ReadingState state = (ReadingState) key.attachment();
        SocketChannel ch = (SocketChannel) key.channel();

        int cnt = ch.read(state.inBuf);

        if (cnt == -1) {
            log.log("Closing connection with a remote node: " + ch);

            ch.close();
            ch.socket().close();
        }

        state.received += cnt;

        state.inBuf.clear();

        if (cnt <= SLOW_RECEIVE_BYTES_THRESHOLD)
            state.slowReceiveStreak++;
        else
            state.slowReceiveStreak = 0;

        if (state.slowReceiveStreak > SLOW_RECEIVE_STREAK_THRESHOLD)
            log.error("A slow connection has been found. Channel: " + ch);
    }

    private void processWrite(SelectionKey key) throws IOException {
        SocketChannel ch = (SocketChannel) key.channel();
        ReadingState state = (ReadingState) key.attachment();

        ch.write(state.greeting);

        if (!state.greeting.hasRemaining()) {
            log.log("Finished sending a greeting to a remote node. Receiving. Channel: " + ch);

            key.interestOps(OP_READ);
        }
    }

    private static class ReadingState {
        int received;
        int slowReceiveStreak;
        final ByteBuffer inBuf;
        final ByteBuffer greeting;

        ReadingState() {
            this.inBuf = ByteBuffer.allocateDirect(INPUT_BUF_SIZE);
            this.greeting = makeGreeting();
        }

        private static ByteBuffer makeGreeting() {
            ByteBuffer buf = ByteBuffer.allocateDirect(GREETING_SIZE);

            for (int i = 0; i < GREETING_SIZE; i++)
                buf.put((byte) i);

            return buf;
        }
    }
}
