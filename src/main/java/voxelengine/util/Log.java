package voxelengine.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {
    private Log() {
    }

    private static final Logger logger = Logger.getLogger(Log.class.getName());

    public static void info(String message) {
        logger.info(message);
    }

    public static void error(String message) {
        logger.log(Level.SEVERE, message);
    }
}
