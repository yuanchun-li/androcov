package xyz.ylimit.androcov;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        if (!Config.parseArgs(args)) {
            return;
        }

        Util.LOGGER.info("OK let's start!");
        try {
            Instrumenter.configSoot();
            Instrumenter.instrument();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
