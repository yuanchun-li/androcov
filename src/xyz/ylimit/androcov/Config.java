package xyz.ylimit.androcov;

/**
 * Created by LiYC on 2015/7/18.
 * Package: DERG
 */
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.jf.util.StringUtils;

import java.io.*;
import java.util.Comparator;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

public class Config {
    public static final String PROJECT_NAME = "androcov";

    // Directory for input
    public static String inputAPK = "";

    // Directory for result output
    public static String outputDirPath = "output";
    public static String tempDirPath = "output/temp";
    public static String outputAPKPath = "";

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
                Config.outputDirPath = cmd.getOptionValue('o');
                File outputDir = new File(Config.outputDirPath);
                Config.outputDirPath = outputDir.getAbsolutePath();
                Config.tempDirPath = String.format("%s/temp", Config.outputDirPath);
                if (!outputDir.exists() && !outputDir.mkdirs()) {
                    throw new ParseException("Error generating output directory.");
                }
                File tempDir = new File(Config.tempDirPath);
                if (tempDir.exists()) {
                    try {
                        FileUtils.forceDelete(tempDir);
                    } catch (IOException e) {
                        throw new ParseException("Error deleting temp directory.");
                    }
                }
                if (!tempDir.mkdirs()) {
                    throw new ParseException("Error generating temp directory.");
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

        File logFile = new File(String.format("%s/androcov.log", Config.outputDirPath));

        try {
            FileHandler fh = new FileHandler(logFile.getAbsolutePath());
            fh.setFormatter(new SimpleFormatter());
            xyz.ylimit.androcov.Util.LOGGER.addHandler(fh);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // get output APK name
        String apkName = new File(inputAPK).getName();
        Config.outputAPKPath = String.format("%s/%s", Config.outputDirPath, apkName);

        xyz.ylimit.androcov.Util.LOGGER.info("finish parsing arguments");
        xyz.ylimit.androcov.Util.LOGGER.info(String.format("[inputAPK]%s, [outputDir]%s", Config.inputAPK, Config.outputDirPath));
        return true;
    }
}