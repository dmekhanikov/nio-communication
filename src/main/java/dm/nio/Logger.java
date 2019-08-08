package dm.nio;

public class Logger {
    private final String name;

    public Logger(String name) {
        this.name = name;
    }

    public void log(String message) {
        System.out.println(format(message));
    }

    public void error(String message) {
        System.err.println(format(message));
    }

    private String format(String message) {
        return "[" + name + "] " + message;
    }
}
