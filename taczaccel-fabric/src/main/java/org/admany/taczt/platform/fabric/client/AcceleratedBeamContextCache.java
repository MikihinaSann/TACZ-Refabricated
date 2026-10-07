package org.admany.taczt.platform.fabric.client;

import com.tacz.guns.compat.ar.BeamRenderContext;

public final class AcceleratedBeamContextCache {
   private static final int SIZE = 8;
   private static final ThreadLocal<Cache> LOCAL = ThreadLocal.withInitial(Cache::new);

   private AcceleratedBeamContextCache() {
   }

   public static BeamRenderContext get(float z, float width, boolean fadeOut) {
      return ((Cache)LOCAL.get()).get(z, width, fadeOut);
   }

   private static final class Cache {
      private final int[] zBits = new int[8];
      private final int[] widthBits = new int[8];
      private final BeamRenderContext[] values = new BeamRenderContext[8];

      private BeamRenderContext get(float z, float width, boolean fadeOut) {
         int zKey = Float.floatToIntBits(z);
         int widthKey = Float.floatToIntBits(width);
         int slot = zKey * 31 + widthKey * 17 + (fadeOut ? 1 : 0) & 7;
         BeamRenderContext cached = this.values[slot];
         if (cached != null && this.zBits[slot] == zKey && this.widthBits[slot] == widthKey && cached.fadeOut() == fadeOut) {
            return cached;
         } else {
            BeamRenderContext created = new BeamRenderContext(z, width, fadeOut);
            this.zBits[slot] = zKey;
            this.widthBits[slot] = widthKey;
            this.values[slot] = created;
            return created;
         }
      }
   }
}
