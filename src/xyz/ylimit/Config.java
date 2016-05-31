package xyz.ylimit;

/**
 * Created by LiYC on 2015/7/18.
 * Package: DERG
 */
import org.apache.commons.cli.*;

import java.io.*;
import java.util.Comparator;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

public class Config {
    public static final String PROJECT_NAME = "androcov";

    // Directory for input
    public static String inputAPK = "";

    // Directory for result output
    public static String outputDir = "output";

    public static String forceAndroidJarPath = "";

    private static PrintStream resultPs;

    public static boolean parseArgs(String[] args) {
        Options options = new Options();
        Option output_opt = Option.builder("o").argName("directory").required()
                .longOpt("output").hasArg().desc("path to output dir").build();
        Option input_opt = Option.builder("i").argName("APK").required()
                .longOpt("input").hasArg().desc("path to target APK").build();
        Option sdk_opt = Option.builder("sdk").argName("android.jar").required()
                .longOpt("android_jar").hasArg().desc("path to android.jar").build();
        Option help_opt = Option.builder("h").desc("print this help message")
                .longOpt("help").build();

        options.addOption(output_opt);
        options.addOption(input_opt);
        options.addOption(sdk_opt);
        options.addOption(help_opt);

        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("i")) {
                Config.inputAPK = cmd.getOptionValue('i');
                File codeDirFile = new File(Config.inputAPK);
                if (!codeDirFile.exists()) {
                    throw new ParseException("Target APK does not exist.");
                }
            }
            if (cmd.hasOption('o')) {
                Config.outputDir = cmd.getOptionValue('o');
                File workingDir = new File(Config.outputDir);
                Config.outputDir = workingDir.getPath();
                if (!workingDir.exists() && !workingDir.mkdirs()) {
                    throw new ParseException("Error generating output directory.");
                }
            }
            if (cmd.hasOption("sdk")) {
                Config.forceAndroidJarPath = cmd.getOptionValue("sdk");
                File androidJarFile = new File(Config.forceAndroidJarPath);
                if (!androidJarFile.exists()) {
                    throw new ParseException("android.jar does not exist.");
                }
            }
            if (cmd.hasOption("h")) {
                throw new ParseException("print help message.");
            }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.setOptionComparator(new Comparator<Option>() {
                @Override
                public int compare(Option o1, Option o2) {
                    return o1.getOpt().length() - o2.getOpt().length();
                }
            });
            formatter.printHelp(Config.PROJECT_NAME, options, true);
            return false;
        }

        File logFile = new File(String.format("%s/androcov.log", Config.outputDir));

        try {
            FileHandler fh = new FileHandler(logFile.getAbsolutePath());
            fh.setFormatter(new SimpleFormatter());
            Util.LOGGER.addHandler(fh);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Util.LOGGER.info("finish parsing arguments");
        Util.LOGGER.info(String.format("[inputAPK]%s, [outputDir]%s", Config.inputAPK, Config.outputDir));
        return true;
    }
}