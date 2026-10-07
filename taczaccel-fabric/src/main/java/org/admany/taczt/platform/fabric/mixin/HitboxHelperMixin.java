package org.admany.taczt.platform.fabric.mixin;

import com.tacz.guns.api.entity.ITargetEntity;
import com.tacz.guns.util.HitboxHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {HitboxHelper.class}
)
public abstract class HitboxHelperMixin {
   @Inject(
      method = {"getFixedBoundingBox"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 1
   )
   private static void taczt$reuseNonPlayerMotion(Entity target, Entity shooter, CallbackInfoReturnable<AABB> callback) {
      if (!(target instanceof Player)) {
         double x = target.getX() - target.xo;
         double y = target.getY() - target.yo;
         double z = target.getZ() - target.zo;
         AABB bounds = target.getBoundingBox().expandTowards(x, y, z);
         if (target.getVehicle() != null || target instanceof ITargetEntity) {
            bounds = bounds.move((double)-2.5F * x, (double)-2.5F * y, (double)-2.5F * z);
         }

         callback.setReturnValue(bounds.move((double)-5.0F * x, (double)-5.0F * y, (double)-5.0F * z));
      }
   }
}
