package xyz.ylimit.androcov;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JIdentityStmt;
import soot.options.Options;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Created by yuanchun on 5/31/16.
 * Package: androcov
 */
public class Instrumenter {
    private static HashSet<String> allMethods = new HashSet<>();

    public static void configSoot() throws IOException {
//        Options.v().set_prepend_classpath(true);
        Options.v().set_allow_phantom_refs(true);
//        Options.v().set_whole_program(true);
        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_output_dir(Config.outputDirPath);
        Options.v().set_debug(true);
        Options.v().set_validate(true);
        Options.v().set_output_format(Options.output_format_dex);

        List<String> process_dirs = new ArrayList<>();
        process_dirs.add(Config.inputAPK);

        URL coverageHelperUrl = Instrumenter.class.getClass().getResource("/CoverageHelper.java");
        String helperDirPath = String.format("%s/CoverageHelper", Config.tempDirPath);
        String helperClassPath = String.format("%s/CoverageHelper/xyz/ylimit/androcov/CoverageHelper.java",
                Config.tempDirPath);
        File helperClassFile = new File(helperClassPath);
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
        final SootClass helperClass = Scene.v().getSootClass("xyz.ylimit.androcov.CoverageHelper");
        final SootMethod helperMethod = helperClass.getMethodByName("reach");

        PackManager.v().getPack("jtp").add(new Transform("jtp.androcov", new BodyTransformer() {
            @Override
            protected void internalTransform(final Body b, String phaseName, @SuppressWarnings("rawtypes") Map options) {
                final PatchingChain units = b.getUnits();
                // important to use snapshotIterator here
                if (b.getMethod().getDeclaringClass() == helperClass) return;
                String methodSig = b.getMethod().getSignature();
                allMethods.add(methodSig);

                // perform instrumentation here
                for(Iterator iter = units.snapshotIterator(); iter.hasNext();) {
                    final Unit u = (Unit) iter.next();
//                    // insert before return statements
//                    if (u instanceof ReturnStmt || u instanceof RetStmt || u instanceof ReturnVoidStmt) {
//                        InvokeStmt logStatement = Jimple.v().newInvokeStmt(
//                                Jimple.v().newStaticInvokeExpr(helperMethod.makeRef(), StringConstant.v(methodSig)));
//                        units.insertBefore(logStatement, u);
//                    }
                    // insert before the first non-identity statement
                    if (!(u instanceof JIdentityStmt)) {
                        InvokeStmt logStatement = Jimple.v().newInvokeStmt(
                                Jimple.v().newStaticInvokeExpr(helperMethod.makeRef(), StringConstant.v(methodSig)));
                        units.insertBefore(logStatement, u);
                        break;
                    }
                }
                b.validate();
            }
        }));
        PackManager.v().runPacks();
        PackManager.v().writeOutput();
        if (new File(Config.outputAPKPath).exists()) {
            Util.LOGGER.info("finish instrumenting");
            Util.signAPK(Config.outputAPKPath);
            Util.LOGGER.info("finish signing");
            Util.LOGGER.info("instrumented apk: " + Config.outputAPKPath);
        }
        else {
            Util.LOGGER.warning("error instrumenting");
        }
    }

    public static void output() {
        HashMap<String, Object> outputMap = new HashMap<>();
        outputMap.put("outputAPK", Config.outputAPKPath);
        outputMap.put("allMethods", allMethods);
        File instrumentResultFile = new File(Config.outputResultPath);
        JSONObject resultJson = new JSONObject(outputMap);
        try {
            FileUtils.writeStringToFile(instrumentResultFile, resultJson.toString(2), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
