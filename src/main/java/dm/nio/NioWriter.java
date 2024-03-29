package dm.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import static dm.nio.Properties.GREETING_SIZE;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

public class NioWriter extends Worker {
    private final Selector selector;
    private final BlockingQueue<WriteRequest> writeRequests;
    private final Runnable finishedCallback;

    public NioWriter(int idx, BlockingQueue<WriteRequest> writeRequests, Runnable finishedCallback) throws IOException {
        super("writer-" + idx);

        this.selector = SelectorProvider.provider().openSelector();
        this.writeRequests = writeRequests;
        this.finishedCallback = finishedCallback;
    }

    @Override
    public void run() {
        log.info("Starting a NIO writer thread.");

        try {
            while (!Thread.interrupted()) {
                WriteRequest req = writeRequests.poll();

                if (req != null)
                    registerWriteRequest(req);

                log.debug("Selecting. Interest ops: " + interestOps(selector));

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

    private void registerWriteRequest(WriteRequest req) throws ClosedChannelException {
        log.info("Starting processing of a new write request. Channel: " + req.channel());

        SelectionKey key = req.channel().register(selector, OP_READ);
        key.attach(new WritingState(req.buffer()));
    }

    private void processRead(SelectionKey key) throws IOException {
        WritingState state = (WritingState) key.attachment();
        SocketChannel ch = (SocketChannel) key.channel();

        log.debug("Processing read [channel=" + ch + ']');

        ch.read(state.greetingBuf);

        if (!state.greetingBuf.hasRemaining()) {
            log.info("A greeting has been received. Writing. Channel: " + ch);

            key.interestOps(OP_WRITE);
        }
    }

    private void processWrite(SelectionKey key) throws IOException {
        WritingState state = (WritingState) key.attachment();
        SocketChannel ch = (SocketChannel) key.channel();

        log.debug("Processing write [channel=" + ch + ']');

        int writeRes = ch.write(state.outBuf);

        if (writeRes == -1)
            throw new ClosedChannelException();

        if (!state.outBuf.hasRemaining()) {
            log.info("Finished sending data to a remote node. Closing the channel: " + ch);

            try {
                closeChannel(ch);
            } finally {
                finishedCallback.run();
            }
        }
    }

    private void closeChannel(SocketChannel ch) throws IOException {
        ch.close();
        ch.socket().close();
    }

    private static class WritingState {
        ByteBuffer outBuf;
        ByteBuffer greetingBuf;

        WritingState(ByteBuffer outBuf) {
            this.outBuf = outBuf;
            this.greetingBuf = ByteBuffer.allocateDirect(GREETING_SIZE);
        }
    }
}
