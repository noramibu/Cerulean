package toni.cerulean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import toni.cerulean.util.Config;

public class Common {

    public static final Logger LOG = LoggerFactory.getLogger("Cerulean");

    public static final Config config = new Config();

    public static void init() {
    }
}