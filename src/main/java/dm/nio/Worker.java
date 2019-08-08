package dm.nio;

public abstract class Worker extends Thread {
    protected final Logger log;

    public Worker(String name) {
        super(name);
        this.log = new Logger(name);
    }

    public abstract void run();
}
