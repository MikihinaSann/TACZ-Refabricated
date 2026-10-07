package org.admany.taczt.platform.fabric.mixin.client;

import com.tacz.guns.client.resource.InternalAssetLoader;
import org.admany.taczt.platform.fabric.client.RenderReloadLifecycle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {InternalAssetLoader.class}
)
public abstract class InternalAssetLoaderMixin {
   @Inject(
      method = {"onResourceReload"},
      at = {@At("HEAD")},
      require = 1,
      remap = false
   )
   private static void taczt$invalidateBeforeReload(CallbackInfo callback) {
      RenderReloadLifecycle.begin();
   }

   @Inject(
      method = {"onResourceReload"},
      at = {@At("RETURN")},
      require = 1,
      remap = false
   )
   private static void taczt$invalidateAfterReload(CallbackInfo callback) {
      RenderReloadLifecycle.end();
   }
}
