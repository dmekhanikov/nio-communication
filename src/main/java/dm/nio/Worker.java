package dm.nio;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.nio.channels.SelectionKey.*;

public abstract class Worker extends Thread {
    protected final Logger log;

    public Worker(String name) {
        super(name);
        this.log = new Logger(name);
    }

    public abstract void run();

    protected List<List<String>> interestOps(Selector selector) {
        Set<SelectionKey> keys = selector.keys();
        List<List<String>> res = new ArrayList<>(keys.size());

        for (SelectionKey key : keys) {
            if (key.isValid())
                res.add(interestOps(key));
        }

        return res;
    }

    protected List<String> interestOps(SelectionKey key) {
        List<String> ops = new ArrayList<>();

        int interestOps = key.interestOps();

        if ((interestOps & OP_READ) != 0)
            ops.add("OP_READ");
        if ((interestOps & OP_WRITE) != 0)
            ops.add("OP_WRITE");
        if ((interestOps & OP_ACCEPT) != 0)
            ops.add("OP_ACCEPT");
        if ((interestOps & OP_CONNECT) != 0)
            ops.add("OP_CONNECT");

        return ops;
    }
}
