package org.admany.taczt.platform.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import net.minecraft.world.item.ItemDisplayContext;
import org.admany.taczt.platform.fabric.client.BedrockPoseStackPool;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {BedrockPart.class}
)
public abstract class BedrockPartMixin {
   private static final String TACZT$FULL_RENDER = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V";
   @Shadow
   public boolean visible;

   @Inject(
      method = {"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 1
   )
   private void taczt$skipHiddenPart(PoseStack poseStack, ItemDisplayContext context, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha, CallbackInfo callbackInfo) {
      if (!this.visible) {
         callbackInfo.cancel();
      }

   }

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

   @Redirect(
      method = {"translateAndRotateAndScale"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V",
   ordinal = 0
),
      require = 1,
      remap = true
   )
   private void taczt$skipZeroAnimationOffset(PoseStack poseStack, float x, float y, float z) {
      if (x != 0.0F || y != 0.0F || z != 0.0F) {
         poseStack.translate(x, y, z);
      }

   }

   @Redirect(
      method = {"translateAndRotateAndScale"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V",
   ordinal = 1
),
      require = 1,
      remap = true
   )
   private void taczt$skipZeroBoneTranslation(PoseStack poseStack, float x, float y, float z) {
      if (x != 0.0F || y != 0.0F || z != 0.0F) {
         poseStack.translate(x, y, z);
      }

   }

   @Redirect(
      method = {"translateAndRotateAndScale"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V",
   ordinal = 3
),
      require = 1,
      remap = true
   )
   private void taczt$skipIdentityAdditionalRotation(PoseStack poseStack, Quaternionf rotation) {
      if (rotation.x != 0.0F || rotation.y != 0.0F || rotation.z != 0.0F || rotation.w != 1.0F) {
         poseStack.mulPose(rotation);
      }

   }

   @Redirect(
      method = {"translateAndRotateAndScale"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"
),
      require = 1,
      remap = true
   )
   private void taczt$skipIdentityScale(PoseStack poseStack, float x, float y, float z) {
      if (x != 1.0F || y != 1.0F || z != 1.0F) {
         poseStack.scale(x, y, z);
      }

   }
}
