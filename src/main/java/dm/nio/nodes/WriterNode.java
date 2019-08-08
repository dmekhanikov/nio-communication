package dm.nio.nodes;

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
    public static void main(String[] args) throws IOException, InterruptedException {
        BlockingQueue<WriteRequest> writeRequests = new ArrayBlockingQueue<>(WRITE_REQUESTS_NUM);
        Semaphore sem = new Semaphore(WRITE_REQUESTS_NUM);

        for (int i = 0; i < WRITERS_NUM; i++) {
            NioWriter writer = new NioWriter(i, writeRequests, sem::release);
            writer.start();
        }

        while (!Thread.interrupted()) {
            sem.acquire();

            SocketChannel ch = SocketChannel.open(new InetSocketAddress(HOST, PORT));
            ch.configureBlocking(false);
            ch.socket().setReceiveBufferSize(Properties.BUFFER_SIZE);

            WriteRequest req = new WriteRequest(ch, createOutputData());
            writeRequests.add(req);
        }
    }

    private static ByteBuffer createOutputData() {
        ByteBuffer buf = ByteBuffer.allocateDirect(DATA_SIZE);

        for (int i = 0; i < DATA_SIZE; i++)
            buf.put((byte) i);

        return buf;
    }
}
