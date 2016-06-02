package xyz.ylimit.androcov;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by liyc on 12/23/15.
 * useful utils
 */
public class Util {
    public static final Logger LOGGER = Logger.getLogger(Config.PROJECT_NAME);

    public static String getTimeString() {
        long timeMillis = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-hhmmss");
        Date date = new Date(timeMillis);
        return sdf.format(date);
    }

    public static void logException(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        Util.LOGGER.warning(sw.toString());
    }

    public static float safeDivide(int obfuscated, int total) {
        if (total <= 0) return 1;
        return (float) obfuscated / total;
    }

    public static void signAPK(String apkPath) {
        Runtime r = Runtime.getRuntime();
        URL keystoreUrl = Instrumenter.class.getClass().getResource("/debug.keystore");
        String keystorePath = String.format("%s/debug.keystore", Config.tempDirPath);
        String signCmd = String.format(
                "jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -storepass android " +
                        "-keystore %s %s androiddebugkey", keystorePath, apkPath);
        try {
            FileUtils.copyURLToFile(keystoreUrl, new File(keystorePath));
            Process p = r.exec(signCmd);
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
