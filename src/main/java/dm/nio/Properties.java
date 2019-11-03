package dm.nio;

public class Properties {
    public static final int PORT = getInteger("PORT", 57100);
    public static final int BUFFER_SIZE = getInteger("BUFFER_SIZE", 32 * 1024);
    public static final String HOST = getString("HOST", "localhost");

    public static final int GREETING_SIZE = getInteger("GREETING_SIZE", 18);

    public static final int READERS_NUM = getInteger("READERS_NUM", 4);

    public static final int INPUT_BUF_SIZE = getInteger("INPUT_BUF_SIZE", 32 * 1024);
    public static final int SLOW_RECEIVE_BYTES_THRESHOLD = getInteger("SLOW_RECEIVE_BYTES_THRESHOLD", 500);
    public static final int SLOW_RECEIVE_STREAK_THRESHOLD = getInteger("SLOW_RECEIVE_STREAK_THRESHOLD", 100);

    public static final int WRITERS_NUM = getInteger("WRITERS_NUM", 4);
    public static final int WRITE_REQUESTS_NUM = getInteger("WRITE_REQUESTS_NUM", 16);
    public static final int DATA_SIZE = getInteger("DATA_SIZE", 100 * 1024 * 1024);

    public static Integer getInteger(String propName, Integer def) {
        String val = System.getProperty(propName);

        if (val == null)
            return def;
        else
            return Integer.parseInt(val);
    }

    public static String getString(String propName, String def) {
        return System.getProperty(propName, def);
    }

    public static boolean getBoolean(String propName, boolean def) {
        String val = System.getProperty(propName);

        if (val == null)
            return def;
        else
            return Boolean.parseBoolean(val);
    }
}
