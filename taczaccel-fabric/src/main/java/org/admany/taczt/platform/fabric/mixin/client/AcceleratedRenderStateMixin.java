package org.admany.taczt.platform.fabric.mixin.client;

import org.admany.taczt.platform.fabric.client.AcceleratedRenderDecision;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   targets = {"com.github.argon4w.acceleratedrendering.core.CoreFeature"}
)
@Pseudo
public abstract class AcceleratedRenderStateMixin {
   @Inject(
      method = {"setRenderingLevel", "resetRenderingLevel", "setRenderingHand", "resetRenderingHand", "setRenderingGui", "resetRenderingGui"},
      at = {@At("HEAD")},
      require = 0
   )
   private static void taczt$invalidateDecision(CallbackInfo callbackInfo) {
      AcceleratedRenderDecision.invalidate();
   }
}
