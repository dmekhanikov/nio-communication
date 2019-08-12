package dm.nio.nodes;

import dm.nio.Acceptor;
import dm.nio.NioReader;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static dm.nio.Properties.READERS_NUM;

public class ReaderNode {
    public static void main(String[] args) throws IOException {
        new ReaderNode().run();
    }

    void run() throws IOException {
        BlockingQueue<SocketChannel> channels = new LinkedBlockingQueue<>();

        for (int i = 0; i < READERS_NUM; i++) {
            NioReader reader = new NioReader(i, channels);
            reader.start();
        }

        Thread acceptorThread = new Acceptor(channels::add);
        acceptorThread.start();
    }
}
