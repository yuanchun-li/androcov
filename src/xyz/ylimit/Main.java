package xyz.ylimit;

public class Main {

    public static void main(String[] args) {
        if (!Config.parseArgs(args)) {
            return;
        }

        Util.LOGGER.info("OK let's start!");
    }
}
