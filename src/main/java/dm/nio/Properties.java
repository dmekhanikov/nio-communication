package dm.nio;

public class Properties {
    public static final int PORT = 57100;
    public static final int BUFFER_SIZE = 32 * 1024;
    public static final String HOST = "localhost";

    public static final int GREETING_SIZE = 18;

    public static final int READERS_NUM = 4;

    public static final int INPUT_BUF_SIZE = 32 * 1024;
    public static final int SLOW_RECEIVE_BYTES_THRESHOLD = 500;
    public static final int SLOW_RECEIVE_STREAK_THRESHOLD = 100;

    public static final int WRITERS_NUM = 4;
    public static final int WRITE_REQUESTS_NUM = 16;
    public static final int DATA_SIZE = 100 * 1024 * 1024;
}
