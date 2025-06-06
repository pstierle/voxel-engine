package voxelengine;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import static org.lwjgl.opengl.GL11.glGetIntegerv;

public class SystemUtil {

    private static final int GL_GPU_MEMORY_INFO_TOTAL_AVAILABLE_MEMORY_NVX = 0x9048;
    private static final int GL_GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX = 0x9049;

    public static void printSystemInfo() {
        // === JVM Memory ===
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        System.out.printf("JVM Memory: Used = %.2f MB, Free = %.2f MB, Max = %.2f MB%n",
                usedMemory / (1024.0 * 1024.0),
                freeMemory / (1024.0 * 1024.0),
                maxMemory / (1024.0 * 1024.0));

        // === CPU Info ===
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        System.out.println("Available processors (cores): " + runtime.availableProcessors());
        System.out.println("System load average (1m): " + osBean.getSystemLoadAverage());

        // === GPU Memory Info (NVIDIA only) ===
        try {
            int totalMem = glGetInteger(GL_GPU_MEMORY_INFO_TOTAL_AVAILABLE_MEMORY_NVX);
            int availMem = glGetInteger(GL_GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX);
            int usedMem = totalMem - availMem;

            System.out.printf("GPU Memory (NVIDIA): Used = %d MB, Free = %d MB, Total = %d MB%n",
                    usedMem / 1024, availMem / 1024, totalMem / 1024);
        } catch (Exception e) {
            System.out.println("GPU memory info not available (extension may not be supported).");
        }
    }

    private static int glGetInteger(int name) {
        int[] buffer = new int[1];
        glGetIntegerv(name, buffer);
        return buffer[0];
    }
}