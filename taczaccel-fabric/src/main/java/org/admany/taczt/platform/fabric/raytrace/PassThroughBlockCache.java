package org.admany.taczt.platform.fabric.raytrace;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.resources.ResourceLocation;
import org.admany.quantified.api.compute.QuantifiedCompute;
import org.admany.quantified.api.compute.QuantifiedCompute.Complexity;
import org.admany.quantified.api.compute.QuantifiedCompute.WorkloadKind;

public final class PassThroughBlockCache {
   private static final String REDIRECT_PLACEHOLDER = "";
   private static final ThreadLocal<ResourceLocation> CURRENT_BLOCK = new ThreadLocal();
   private static final AtomicLong PREPARATION_GENERATION = new AtomicLong();
   private static volatile Snapshot snapshot = PassThroughBlockCache.Snapshot.empty();

   private PassThroughBlockCache() {
   }

   public static String capture(ResourceLocation id) {
      if (id == null) {
         CURRENT_BLOCK.remove();
      } else {
         CURRENT_BLOCK.set(id);
      }

      return "";
   }

   public static boolean contains(List<String> configuredIds, Object originalArgument) {
      ResourceLocation blockId = (ResourceLocation)CURRENT_BLOCK.get();
      CURRENT_BLOCK.remove();
      if (blockId == null) {
         return configuredIds.contains(originalArgument);
      } else {
         Snapshot current = snapshot;
         return current.source == configuredIds ? current.ids.contains(blockId) : configuredIds.contains(blockId.toString());
      }
   }

   public static void prepare(List<String> configuredIds) {
      long generation = PREPARATION_GENERATION.incrementAndGet();
      snapshot = PassThroughBlockCache.Snapshot.empty();
      CURRENT_BLOCK.remove();
      List<String> detachedIds = new ArrayList(configuredIds);
      long dataSizeBytes = Math.max(1024L, (long)detachedIds.size() * 64L);
      QuantifiedCompute.builder("taczaccelerated", "tacz-pass-through-config", generation).cpuFallback(() -> buildIds(detachedIds)).dataSizeBytes(dataSizeBytes).parallelUnits(1).complexity(Complexity.SIMPLE).kind(WorkloadKind.GENERAL).cpuOnly().allowMainThreadRerouting(false).submit().whenComplete((parsed, failure) -> {
         if (failure == null && parsed instanceof Set<?> raw && PREPARATION_GENERATION.get() == generation) {
            @SuppressWarnings("unchecked")
            Set<ResourceLocation> parsedIds = (Set<ResourceLocation>) raw;
            snapshot = new Snapshot(configuredIds, parsedIds);
         }

      });
   }

   public static void invalidate() {
      PREPARATION_GENERATION.incrementAndGet();
      snapshot = PassThroughBlockCache.Snapshot.empty();
      CURRENT_BLOCK.remove();
   }

   static void installForTest(List<String> configuredIds) {
      snapshot = new Snapshot(configuredIds, buildIds(configuredIds));
   }

   private static Set<ResourceLocation> buildIds(List<String> configuredIds) {
      Set<ResourceLocation> parsed = new HashSet(Math.max(4, configuredIds.size() * 2));

      for(String configuredId : configuredIds) {
         ResourceLocation parsedId = configuredId == null ? null : ResourceLocation.tryParse(configuredId);
         if (parsedId != null) {
            parsed.add(parsedId);
         }
      }

      return Set.copyOf(parsed);
   }

   private static record Snapshot(List<String> source, Set<ResourceLocation> ids) {
      private static Snapshot empty() {
         return new Snapshot((List)null, Set.of());
      }
   }
}
