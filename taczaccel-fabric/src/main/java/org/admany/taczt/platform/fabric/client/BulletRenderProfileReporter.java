package org.admany.taczt.platform.fabric.client;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public final class BulletRenderProfileReporter {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean ENABLED =
            Boolean.getBoolean("taczt.profile.render") || Boolean.getBoolean("taczt.profile.bullets");
    private static int ticks;

    private BulletRenderProfileReporter() {
    }

    public static void onClientTick() {
        if (ENABLED && ++ticks >= 100) {
            ticks = 0;
            BulletRenderProfiler.Snapshot snapshot = BulletRenderProfiler.snapshotAndReset();
            GunRenderProfiler.Snapshot firstPerson = GunRenderProfiler.snapshotFirstPersonAndReset();
            GunRenderProfiler.Snapshot item = GunRenderProfiler.snapshotItemAndReset();
            if (snapshot.renders() > 0L || firstPerson.renders() > 0L || item.renders() > 0L) {
                LOGGER.info(
                        "[TaCZ:Accel] bullet-render samples={}, totalMs={}, averageUs={}, maxUs={}",
                        snapshot.renders(), snapshot.totalMillis(), snapshot.averageMicros(), snapshot.maximumMicros());
                LOGGER.info(
                        "[TaCZ:Accel] gun-render firstPerson(samples={}, totalMs={}, averageUs={}, maxUs={}) item(samples={}, totalMs={}, averageUs={}, maxUs={})",
                        firstPerson.renders(), firstPerson.totalMillis(), firstPerson.averageMicros(), firstPerson.maximumMicros(),
                        item.renders(), item.totalMillis(), item.averageMicros(), item.maximumMicros());
            }
        }
    }
}
