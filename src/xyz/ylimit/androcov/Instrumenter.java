package xyz.ylimit.androcov;

import org.apache.commons.io.FileUtils;
import soot.*;
import soot.jimple.*;
import soot.options.Options;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by yuanchun on 5/31/16.
 * Package: androcov
 */
public class Instrumenter {
    public static void configSoot() throws IOException {
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

        URL coverageHelperUrl = Instrumenter.class.getClass().getResource("/CoverageHelper.java");
        String helperDirPath = String.format("%s/CoverageHelper", Config.outputDir);
        String helperClassPath = String.format("%s/CoverageHelper/xyz/ylimit/androcov/CoverageHelper.java",
                Config.outputDir);
        File helpDir = new File(helperDirPath);
        File helperClassFile = new File(helperClassPath);
        FileUtils.forceDelete(helpDir);
        if (!helperClassFile.getParentFile().mkdirs())
            throw new IOException("Fail to create directory");
        FileUtils.copyURLToFile(coverageHelperUrl, helperClassFile);
        process_dirs.add(helperDirPath);

        Options.v().set_process_dir(process_dirs);
        Options.v().set_force_android_jar(Config.forceAndroidJarPath);
        Options.v().set_force_overwrite(true);
    }

    public static void instrument() {
        Util.LOGGER.info("Start instrumenting...");

        Scene.v().loadNecessaryClasses();
        final SootMethod reachMethod = Scene.v().getSootClass("xyz.ylimit.androcov.CoverageHelper").getMethodByName("reach");

        PackManager.v().getPack("jtp").add(new Transform("jtp.androcov", new BodyTransformer() {
            @Override
            protected void internalTransform(final Body b, String phaseName, @SuppressWarnings("rawtypes") Map options) {
                final PatchingChain units = b.getUnits();
                // important to use snapshotIterator here
                String methodSig = b.getMethod().getSignature();
                // String logStr = String.format("Instrumenting: %s", methodSig);
                // Util.LOGGER.info(logStr);
                // perform instrumentation here

                for(Iterator iter = units.snapshotIterator(); iter.hasNext();) {
                    final Unit u = (Unit) iter.next();
                    if (u instanceof ReturnStmt || u instanceof RetStmt || u instanceof ReturnVoidStmt) {
                        InvokeStmt logStatement = Jimple.v().newInvokeStmt(
                                Jimple.v().newStaticInvokeExpr(reachMethod.makeRef(), StringConstant.v(methodSig)));
                        units.insertBefore(logStatement, u);
                    }
                }
                b.validate();
            }
        }));
        PackManager.v().runPacks();
        PackManager.v().writeOutput();
    }
}
