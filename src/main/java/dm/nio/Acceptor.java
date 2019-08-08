package dm.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;
import java.util.function.Consumer;

public class Acceptor extends Thread {
    private Selector selector;
    private Consumer<SocketChannel> channelCallback;

    public Acceptor(Consumer<SocketChannel> channelCallback) throws IOException {
        super("acceptor");

        this.selector = createAcceptSelector(new InetSocketAddress(Properties.HOST, Properties.PORT));

        this.channelCallback = channelCallback;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                SocketChannel ch = accept();

                channelCallback.accept(ch);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SocketChannel accept() throws IOException {
        selector.select();
        return processSelectedKeys(selector.selectedKeys());
    }

    private SocketChannel processSelectedKeys(Set<SelectionKey> keys) throws IOException {
        if (keys.isEmpty())
            return null;

        assert keys.size() == 1;

        SelectionKey key = keys.iterator().next();

        if (!key.isValid())
            return null;

        if (key.isAcceptable()) {
            ServerSocketChannel srvrCh = (ServerSocketChannel) key.channel();

            SocketChannel sockCh = srvrCh.accept();

            sockCh.configureBlocking(false);
            sockCh.socket().setTcpNoDelay(true);
            sockCh.socket().setKeepAlive(true);

            sockCh.socket().setSendBufferSize(Properties.BUFFER_SIZE);
            sockCh.socket().setReceiveBufferSize(Properties.BUFFER_SIZE);

            return sockCh;
        } else
            return null;
    }

    private Selector createAcceptSelector(SocketAddress addr) throws IOException {
        Selector selector = SelectorProvider.provider().openSelector();

        ServerSocketChannel acceptCh = ServerSocketChannel.open();
        acceptCh.configureBlocking(false);
        acceptCh.socket().setReceiveBufferSize(Properties.BUFFER_SIZE);
        acceptCh.socket().bind(addr);
        acceptCh.register(selector, SelectionKey.OP_ACCEPT);

        return selector;
    }
}
