package voxelengine.util;

public class Log {
    private Log() {
    }

    public static void info(String message) {
        System.out.println(String.format("[INFO] %s", message));
    }

    public static void error(String message) {
        System.out.println(String.format("[INFO] %s", message));
    }
}
