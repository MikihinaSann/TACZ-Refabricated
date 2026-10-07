package org.admany.taczt.platform.fabric.mixin.client;

import com.tacz.guns.client.resource.ClientIndexManager;
import org.admany.taczt.platform.fabric.client.RenderReloadLifecycle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {ClientIndexManager.class}
)
public abstract class ClientIndexManagerMixin {
   @Inject(
      method = {"reload"},
      at = {@At("HEAD")},
      require = 1,
      remap = false
   )
   private static void taczt$beginReload(CallbackInfo callback) {
      RenderReloadLifecycle.begin();
   }

   @Inject(
      method = {"reload"},
      at = {@At("RETURN")},
      require = 1,
      remap = false
   )
   private static void taczt$finishReload(CallbackInfo callback) {
      RenderReloadLifecycle.end();
   }
}
