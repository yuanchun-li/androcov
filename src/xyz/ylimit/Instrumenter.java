package xyz.ylimit;

import soot.*;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.InvokeStmt;
import soot.options.Options;
import soot.util.Chain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by yuanchun on 5/31/16.
 * Package: androcov
 */
public class Instrumenter {
    public static void configSoot() {
//        Options.v().set_prepend_classpath(true);
        Options.v().set_allow_phantom_refs(true);
//        Options.v().set_whole_program(true);
        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_output_dir(Config.outputDir);
        Options.v().set_debug(true);
        Options.v().set_validate(true);
        Options.v().set_output_format(Options.output_format_dex);

        List<String> process_dirs = new ArrayList<>();
        process_dirs.add(Config.inputAPK);
        Options.v().set_process_dir(process_dirs);
        Options.v().set_force_android_jar(Config.forceAndroidJarPath);
    }

    public static void instrument() {
        Util.LOGGER.info("Start instrumenting...");

        Scene.v().loadNecessaryClasses();

        PackManager.v().getPack("jtp").add(new Transform("jtp.androcov", new BodyTransformer() {

            @Override
            protected void internalTransform(final Body b, String phaseName, @SuppressWarnings("rawtypes") Map options) {
            final PatchingChain units = b.getUnits();
            //important to use snapshotIterator here
            String methodSig = b.getMethod().getSignature();
            String logStr = String.format("Instrumenting: %s", methodSig);
            Util.LOGGER.info(logStr);
            // TODO perform instrumentation here
            }
        }));
        PackManager.v().runPacks();
        PackManager.v().writeOutput();
    }
}
