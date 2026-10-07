package org.admany.taczt.platform.fabric.mixin.client;

import com.tacz.guns.compat.ar.ARCompatImpl;
import com.tacz.guns.compat.ar.BeamRenderContext;
import org.admany.taczt.platform.fabric.client.AcceleratedBeamContextCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(
   value = {ARCompatImpl.class}
)
public abstract class AcceleratedBeamRendererMixin {
   @Redirect(
      method = {"renderLaser"},
      at = @At(
   value = "NEW",
   target = "Lcom/tacz/guns/compat/ar/BeamRenderContext;"
),
      require = 1
   )
   private static BeamRenderContext taczt$reuseBeamContext(float z, float width, boolean fadeOut) {
      return AcceleratedBeamContextCache.get(z, width, fadeOut);
   }
}
