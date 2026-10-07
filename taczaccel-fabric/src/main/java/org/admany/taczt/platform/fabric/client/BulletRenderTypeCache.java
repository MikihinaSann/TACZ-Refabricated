package org.admany.taczt.platform.fabric.client;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class BulletRenderTypeCache {
   private static final int MAX_ENTRIES = 64;
   private static final Map<Key, RenderType> TRACER_TYPES = new LinkedHashMap<Key, RenderType>(16, 0.75F, true) {
      protected boolean removeEldestEntry(Map.Entry<Key, RenderType> eldest) {
         return this.size() > 64;
      }
   };

   private BulletRenderTypeCache() {
   }

   public static RenderType tracerEnergySwirl(ResourceLocation texture, float uOffset, float vOffset) {
      Key key = new Key(texture, Float.floatToIntBits(uOffset), Float.floatToIntBits(vOffset));
      synchronized(TRACER_TYPES) {
         RenderType cached = (RenderType)TRACER_TYPES.get(key);
         if (cached != null) {
            return cached;
         } else {
            RenderType created = RenderType.energySwirl(texture, uOffset, vOffset);
            TRACER_TYPES.put(key, created);
            return created;
         }
      }
   }

   public static void clear() {
      synchronized(TRACER_TYPES) {
         TRACER_TYPES.clear();
      }
   }

   private static record Key(ResourceLocation texture, int uOffset, int vOffset) {
   }
}
