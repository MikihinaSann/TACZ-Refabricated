package org.admany.taczt.platform.fabric.mixin.client;

import com.tacz.guns.client.model.bedrock.BedrockPart;
import java.util.Map;
import org.admany.taczt.platform.fabric.client.AcceleratedMeshLookupCache;
import org.admany.taczt.platform.fabric.client.AcceleratedRenderDecision;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(
   value = {BedrockPart.class}
)
public abstract class AcceleratedBedrockPartCacheMixin {
   @Redirect(
      method = {"render"},
      at = @At(
   value = "INVOKE",
   target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"
),
      require = 0
   )
   private Object taczt$lookupMesh(Map<?, ?> map, Object key) {
      return AcceleratedMeshLookupCache.get(map, key);
   }

   @Redirect(
      method = {"render"},
      at = @At(
   value = "INVOKE",
   target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
),
      require = 0
   )
   private Object taczt$publishMesh(Map<Object, Object> map, Object key, Object value) {
      return AcceleratedMeshLookupCache.put(map, key, value);
   }

   @Redirect(
      method = {"compileFast"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/tacz/guns/compat/ar/ARCompat;shouldAccelerate()Z"
),
      require = 0
   )
   private boolean taczt$cachedAccelerationDecision() {
      return AcceleratedRenderDecision.shouldAccelerate();
   }
}
