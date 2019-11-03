package dm.nio.nodes;

import dm.nio.Logger;
import dm.nio.NioWriter;
import dm.nio.Properties;
import dm.nio.WriteRequest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import static dm.nio.Properties.*;

public class WriterNode {
    private Logger log = new Logger("writer-node");

    public static void main(String[] args) throws IOException, InterruptedException {
        new WriterNode().run();
    }

    void run() throws IOException, InterruptedException {
        BlockingQueue<WriteRequest> writeRequests = new ArrayBlockingQueue<>(WRITE_REQUESTS_NUM);
        Semaphore sem = new Semaphore(WRITE_REQUESTS_NUM);

        for (int i = 0; i < WRITERS_NUM; i++) {
            int idx = i;

            NioWriter writer = new NioWriter(idx, writeRequests, () -> {
                Logger log = new Logger("writer-" + idx);

                log.debug("Releasing the semaphore [sem=" + sem + ']');

                sem.release();
            });
            writer.start();
        }

        while (!Thread.interrupted()) {
            sem.acquire();

            log.debug("Semaphore acquired [sem=" + sem + "]. Creating a request.");

            SocketChannel ch = SocketChannel.open(new InetSocketAddress(HOST, PORT));
            ch.configureBlocking(false);
            ch.socket().setReceiveBufferSize(Properties.BUFFER_SIZE);

            WriteRequest req = new WriteRequest(ch, createOutputData());
            writeRequests.add(req);
        }
    }

    private ByteBuffer createOutputData() {
        ByteBuffer buf = ByteBuffer.allocateDirect(DATA_SIZE);

        for (int i = 0; i < DATA_SIZE; i++)
            buf.put((byte) i);

        return buf;
    }
}
