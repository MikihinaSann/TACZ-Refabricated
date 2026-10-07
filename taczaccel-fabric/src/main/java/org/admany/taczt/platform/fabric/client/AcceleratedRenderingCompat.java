package org.admany.taczt.platform.fabric.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tacz.guns.compat.ar.ARCompat;
import net.fabricmc.loader.api.FabricLoader;

public final class AcceleratedRenderingCompat {
    private static final boolean LOADED = FabricLoader.getInstance().isModLoaded("acceleratedrendering");

    private AcceleratedRenderingCompat() {}

    public static boolean isLoaded() {
        return LOADED;
    }

    public static boolean shouldAccelerate() {
        if (!LOADED) return false;
        try {
            return ARCompat.shouldAccelerate();
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean isAccelerated(VertexConsumer consumer) {
        if (!LOADED || consumer == null) return false;
        try {
            return ARCompat.isAccelerated(consumer);
        } catch (Throwable ignored) {
            return false;
        }
    }
}
