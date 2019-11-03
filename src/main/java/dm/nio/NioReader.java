package dm.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import static dm.nio.Properties.*;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

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
        log.info("Starting a NIO reader thread.");

        try {
            while (!Thread.interrupted()) {
                SocketChannel ch = channels.poll();

                if (ch != null)
                    registerChannel(ch);

                selector.select(2000);

                for (Iterator<SelectionKey> iter = selector.selectedKeys().iterator(); iter.hasNext(); ) {
                    SelectionKey key = iter.next();
                    iter.remove();

                    if ((key.interestOps() & OP_READ) != 0 && key.isReadable())
                        processRead(key);
                    else if ((key.interestOps() & OP_WRITE) != 0 && key.isWritable())
                        processWrite(key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerChannel(SocketChannel ch) throws IOException {
        log.info("Registering a new channel: " + ch);

        SelectionKey key = ch.register(selector, OP_WRITE);
        key.attach(new ReadingState());
    }

    private void processRead(SelectionKey key) throws IOException {
        ReadingState state = (ReadingState) key.attachment();
        SocketChannel ch = (SocketChannel) key.channel();

        log.debug("Processing read [channel=" + ch + ']');

        int res = ch.read(state.inBuf);

        if (res == -1) {
            log.info("Closing connection with a remote node: " + ch);

            ch.close();
            ch.socket().close();

            return;
        }

        state.received += res;

        state.inBuf.clear();

        if (res <= SLOW_RECEIVE_BYTES_THRESHOLD)
            state.slowReceiveStreak++;
        else
            state.slowReceiveStreak = 0;

        if (state.slowReceiveStreak > SLOW_RECEIVE_STREAK_THRESHOLD)
            log.error("A slow connection has been found. Channel: " + ch);
    }

    private void processWrite(SelectionKey key) throws IOException {

        SocketChannel ch = (SocketChannel) key.channel();
        ReadingState state = (ReadingState) key.attachment();

        log.debug("Processing write [channel=" + ch + ']');

        int cnt = ch.write(state.greeting);

        log.debug("Bytes sent [sockCh=" + ch + ", cnt=" + cnt + ", buf=" + state.greeting + ']');

        if (!state.greeting.hasRemaining()) {
            log.info("Finished sending a greeting to a remote node. Receiving. Channel: " + ch);

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

            buf.flip();

            return buf;
        }
    }
}
