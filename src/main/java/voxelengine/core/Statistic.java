package voxelengine.core;

import org.joml.Vector3d;
import voxelengine.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class Statistic {
    private int renderedChunkCount = 0;
    private int fps = 0;
    private Vector3d cameraPosition = new Vector3d();
    private Timer statsTimer;

    public void startPrint() {
        statsTimer = new Timer("StatsTimer");

        statsTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                printStats();
            }
        }, 0, 1000);
    }

    public void setCameraPosition(Vector3d cameraPosition) {
        this.cameraPosition = cameraPosition;
    }

    public void setRenderedChunkCount(int count) {
        this.renderedChunkCount = count;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    private void printStats() {
        Log.info("==== STATS ====");
        Log.info("FPS: " + fps);
        Log.info("Rendered Chunks: " + renderedChunkCount);
        Log.info(String.format("Position: (%s, %s, %s)", cameraPosition.x, cameraPosition.y, cameraPosition.z));
    }


    public void shutdown() {
        if (statsTimer != null) {
            statsTimer.cancel();
            statsTimer = null;
        }
    }
}