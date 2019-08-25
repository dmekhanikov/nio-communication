package dm.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
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
        log.log("Starting a NIO writer thread.");

        try {
            while (!Thread.interrupted()) {
                WriteRequest req = writeRequests.poll();

                if (req != null)
                    registerWriteRequest(req);

                selector.select(2000);

                for (SelectionKey key : selector.selectedKeys()) {
                    if ((key.interestOps() & OP_WRITE) != 0 && key.isReadable())
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
        log.log("Starting processing of a new write request. Channel: " + req.channel());

        SelectionKey key = req.channel().register(selector, OP_READ);
        key.attach(new WritingState(req.buffer()));
    }

    private void processRead(SelectionKey key) throws IOException {
        WritingState state = (WritingState) key.attachment();
        SocketChannel ch = (SocketChannel) key.channel();

        ch.read(state.greetingBuf);

        if (!state.greetingBuf.hasRemaining()) {
            log.log("A greeting has been received. Writing. Channel: " + ch);

            key.interestOps(OP_WRITE);
        }
    }

    private void processWrite(SelectionKey key) throws IOException {
        WritingState state = (WritingState) key.attachment();
        SocketChannel ch = (SocketChannel) key.channel();

        int writeRes = ch.write(state.outBuf);

        if (writeRes == -1)
            throw new ClosedChannelException();

        if (!state.outBuf.hasRemaining()) {
            log.log("Finished sending data to a remote node. Closing the channel: " + ch);

            closeChannel(ch);
            finishedCallback.run();
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
