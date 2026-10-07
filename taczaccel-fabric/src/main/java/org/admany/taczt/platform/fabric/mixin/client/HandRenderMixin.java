package org.admany.taczt.platform.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tacz.guns.client.model.functional.LeftHandRender;
import com.tacz.guns.client.model.functional.RightHandRender;
import net.minecraft.world.item.ItemDisplayContext;
import org.admany.taczt.platform.fabric.client.AttachmentPoseStackPool;
import org.admany.taczt.platform.fabric.client.FunctionalMatrixPool;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {LeftHandRender.class, RightHandRender.class}
)
public abstract class HandRenderMixin {
   @Inject(
      method = {"lambda$render$0"},
      at = {@At("HEAD")},
      require = 1
   )
   private static void taczt$enterPoseScope(CallbackInfo callback) {
      AttachmentPoseStackPool.enter();
   }

   @Inject(
      method = {"lambda$render$0"},
      at = {@At("RETURN")},
      require = 1
   )
   private static void taczt$leavePoseScope(CallbackInfo callback) {
      AttachmentPoseStackPool.leave();
   }

   @Inject(
      method = {"lambda$render$0"},
      at = {@At("RETURN")},
      require = 1
   )
   private static void taczt$releaseMatrices(Matrix3f normal, Matrix4f pose, PoseStack ignoredStack, VertexConsumer ignoredConsumer, ItemDisplayContext ignoredContext, int ignoredLight, int ignoredOverlay, CallbackInfo callback) {
      FunctionalMatrixPool.release(normal);
      FunctionalMatrixPool.release(pose);
   }

   @Redirect(
      method = {"render"},
      at = @At(
   value = "NEW",
   target = "Lorg/joml/Matrix3f;"
),
      require = 1
   )
   private static Matrix3f taczt$borrowNormalMatrix(Matrix3fc source) {
      return FunctionalMatrixPool.borrow(source);
   }

   @Redirect(
      method = {"render"},
      at = @At(
   value = "NEW",
   target = "Lorg/joml/Matrix4f;"
),
      require = 1
   )
   private static Matrix4f taczt$borrowPoseMatrix(Matrix4fc source) {
      return FunctionalMatrixPool.borrow(source);
   }

   @Redirect(
      method = {"lambda$render$0"},
      at = @At(
   value = "NEW",
   target = "Lcom/mojang/blaze3d/vertex/PoseStack;"
),
      require = 1
   )
   private static PoseStack taczt$borrowPoseStack() {
      return AttachmentPoseStackPool.borrow();
   }
}
