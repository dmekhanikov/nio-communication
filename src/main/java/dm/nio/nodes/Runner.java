package dm.nio.nodes;

import java.io.IOException;

public class Runner {
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 1)
            error("Wrong number of arguments: " + args.length);
        switch (args[0]) {
            case "reader":
                new ReaderNode().run();
                break;
            case "writer":
                new WriterNode().run();
                break;
            default:
                error("Unrecognized argument: " + args[0]);
        }

    }

    private static void error(String message) {
        System.err.println(message);
        printUsage();
        System.exit(1);
    }

    private static void printUsage() {
        System.err.println("Usage: \n\tjava -jar nio-node.jar " + underline("node type") +
                "\n\tWhere " + underline("node type") + " can be either " +
                bold("reader") + " or " + bold("writer") + ".");
    }

    private static String underline(String s) {
        return "\033[4m" + s + "\033[0m";
    }

    private static String bold(String s) {
        return "\033[1m" + s + "\033[0m";
    }
}
