package org.admany.taczt.platform.fabric.client;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class RenderReloadLifecycle {
   private static final AtomicBoolean RELOADING = new AtomicBoolean();
   private static final AtomicInteger DEPTH = new AtomicInteger();

   private RenderReloadLifecycle() {
   }

   public static void begin() {
      if (DEPTH.getAndIncrement() == 0) {
         RELOADING.set(true);
         clearRenderState();
      }

   }

   public static void end() {
      int depth = DEPTH.decrementAndGet();
      if (depth <= 0) {
         DEPTH.set(0);
         clearRenderState();
         RELOADING.set(false);
      }

   }

   public static boolean isReloading() {
      return RELOADING.get();
   }

   public static void clearRenderState() {
      BulletModelCache.clear();
      BulletRenderLookupCache.clear();
      BulletRenderTypeCache.clear();
      AcceleratedMeshLookupCache.clear();
      AcceleratedRenderDecision.clear();
      AcceleratedRenderDecision.invalidate();
      BedrockCubeCompiler.clear();
      BedrockPoseStackPool.clear();
      BedrockRotationPool.clear();
      AttachmentPoseStackPool.clear();
   }
}
