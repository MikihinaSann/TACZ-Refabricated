package org.admany.taczt.platform.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.client.renderer.entity.EntityBulletRenderer;
import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.client.renderer.MultiBufferSource;
import org.admany.taczt.platform.fabric.client.BulletRenderProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {EntityBulletRenderer.class}
)
public abstract class EntityBulletRenderProfileMixin {
   @Unique
   private long taczt$profileStart;

   @Inject(
      method = {"render(Lcom/tacz/guns/entity/EntityKineticBullet;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"},
      at = {@At("HEAD")},
      require = 1
   )
   private void taczt$beginRenderProfile(EntityKineticBullet bullet, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, CallbackInfo callback) {
      this.taczt$profileStart = BulletRenderProfiler.begin();
   }

   @Inject(
      method = {"render(Lcom/tacz/guns/entity/EntityKineticBullet;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"},
      at = {@At("RETURN")},
      require = 1
   )
   private void taczt$endRenderProfile(EntityKineticBullet bullet, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, CallbackInfo callback) {
      BulletRenderProfiler.end(this.taczt$profileStart);
   }
}
