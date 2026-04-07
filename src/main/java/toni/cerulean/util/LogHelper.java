package toni.cerulean.util;

import toni.cerulean.Common;
import toni.cerulean.foundation.config.RuntimeOptions;

import java.util.function.Supplier;

public class LogHelper {

    public static void debug(String msg) {
        if (RuntimeOptions.debugMode()) {
            Common.LOG.info(msg);
        }
    }
    
    public static void debug(Supplier<String> msg) {
        if (RuntimeOptions.debugMode()) {
            Common.LOG.info(msg.get());
        }
    }
}
