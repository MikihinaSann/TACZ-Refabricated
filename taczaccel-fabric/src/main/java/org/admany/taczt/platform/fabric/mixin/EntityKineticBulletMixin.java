package org.admany.taczt.platform.fabric.mixin;

import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(
   value = {EntityKineticBullet.class}
)
public abstract class EntityKineticBulletMixin {
   @Redirect(
      method = {"onBulletTick"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/world/phys/Vec3;distanceTo(Lnet/minecraft/world/phys/Vec3;)D"
),
      require = 2,
      remap = true
   )
   private double taczt$compareSquaredDistance(Vec3 start, Vec3 hit) {
      return start.distanceToSqr(hit);
   }
}
