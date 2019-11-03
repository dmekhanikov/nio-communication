package dm.nio;

public class Logger {
    private static final boolean DEBUG = Properties.getBoolean("DEBUG", false);

    private final String name;

    public Logger(String name) {
        this.name = name;
    }

    public void info(String message) {
        System.out.println(format(message));
    }

    public void debug(String message) {
        if (DEBUG)
            System.out.println(format(message));
    }

    public void error(String message) {
        System.err.println(format(message));
    }

    private String format(String message) {
        return "[" + name + "] " + message;
    }
}
