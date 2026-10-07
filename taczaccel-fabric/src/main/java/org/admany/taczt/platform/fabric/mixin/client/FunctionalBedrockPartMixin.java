package org.admany.taczt.platform.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.client.model.FunctionalBedrockPart;
import org.admany.taczt.platform.fabric.client.BedrockPoseStackPool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(
   value = {FunctionalBedrockPart.class}
)
public abstract class FunctionalBedrockPartMixin {
   private static final String TACZT$FULL_RENDER = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V";

   @Redirect(
      method = {"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V"
),
      require = 1,
      remap = true
   )
   private void taczt$enterPooledPoseScope(PoseStack poseStack) {
      BedrockPoseStackPool.enter();
      poseStack.pushPose();
   }

   @Redirect(
      method = {"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"
),
      require = 1,
      remap = true
   )
   private void taczt$leavePooledPoseScope(PoseStack poseStack) {
      poseStack.popPose();
      BedrockPoseStackPool.leave();
   }
}
