package xyz.ylimit.androcov;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
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

            ReadStream s1 = new ReadStream("stdin", p.getInputStream());
            ReadStream s2 = new ReadStream("stderr", p.getErrorStream());
            s1.start();
            s2.start();

            p.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

/**
 * Created by yangzy on 11/14/16.
 * to solve Runtime.exec hang problem
 */
class ReadStream implements Runnable {
    String name;
    InputStream is;
    Thread thread;
    public ReadStream(String name, InputStream is) {
        this.name = name;
        this.is = is;
    }
    public void start () {
        thread = new Thread (this);
        thread.start ();
    }
    public void run () {
        try {
            InputStreamReader isr = new InputStreamReader (is);
            BufferedReader br = new BufferedReader (isr);
            while (true) {
                String s = br.readLine ();
                if (s == null) break;
                System.out.println ("[" + name + "] " + s);
            }
            is.close ();
        } catch (Exception ex) {
            System.out.println ("Problem reading stream " + name + "... :" + ex);
            ex.printStackTrace ();
        }
    }
}
