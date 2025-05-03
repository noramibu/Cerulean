package toni.cerulean.util;

import toni.cerulean.Common;

import java.util.function.Supplier;

public class LogHelper {

    private static boolean initialized = false;
    private static boolean debugEnabled = false;

    private static void init() {
        if (!initialized && Common.config != null) {
            initialized = true;
            debugEnabled = Common.config.DEBUG_MODE;
        }
    }

    public static void debug(String msg) {
        init();
        if (debugEnabled) {
            Common.LOG.info(msg);
        }
    }
    
    public static void debug(Supplier<String> msg) {
        init();
        if (debugEnabled) {
            Common.LOG.info(msg.get());
        }
    }
}
