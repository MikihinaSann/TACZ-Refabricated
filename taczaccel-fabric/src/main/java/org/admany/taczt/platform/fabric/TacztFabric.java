package org.admany.taczt.platform.fabric;

import com.mojang.logging.LogUtils;
import com.tacz.guns.config.common.AmmoConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.admany.taczt.platform.fabric.raytrace.PassThroughBlockCache;
import org.slf4j.Logger;

import java.util.List;

public final class TacztFabric implements ModInitializer {
    public static final String MOD_ID = "taczaccelerated";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void logBenchmark(String summary) {
        LOGGER.info("[TaCZ:Accel] benchmark result {}", summary);
    }

    @Override
    public void onInitialize() {
        if (FabricLoader.getInstance().isModLoaded("taczexpands")) {
            LOGGER.info("[TaCZ:Accel] Arcana detected. Shared geometry, transform, lookup, and allocation optimizations remain active. Arcana-owned model and attachment rewrites are left untouched.");
        } else {
            LOGGER.info("[TaCZ:Accel] Shared geometry, transform, lookup, and allocation optimizations active.");
        }

        refreshPassThroughCache();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void refreshPassThroughCache() {
        try {
            PassThroughBlockCache.prepare((List) AmmoConfig.PASS_THROUGH_BLOCKS.get());
        } catch (Throwable error) {
            // Forge Config API Port may not have materialized TaCZ config yet.
            // Lookups remain correct via the cache's source-mismatch fallback.
            LOGGER.debug("[TaCZ:Accel] pass-through cache will use fallback until TaCZ config is available", error);
        }
    }
}
