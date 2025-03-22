package voxelengine.core;

import voxelengine.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class Statistic {
    private final Timer timer;

    public Statistic() {
        this.timer = new Timer("StatsTimer");
    }

    public void startPrint() {
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                printStats();
            }
        }, 0, 1000);
    }

    private void printStats() {
        Log.info("==== STATS ====");
        Log.info("FPS: " + State.renderer.getFrameCount());
        Log.info("Rendered Chunks: " + State.world.getRenderedChunks());
        Log.info(String.format("Position: (%s, %s, %s)", State.camera.position.x, State.camera.position.y, State.camera.position.z));
    }

    public void destroy() {
        this.timer.cancel();
    }
}