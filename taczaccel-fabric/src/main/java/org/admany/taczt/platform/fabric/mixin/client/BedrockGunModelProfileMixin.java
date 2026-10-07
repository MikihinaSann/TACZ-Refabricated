package org.admany.taczt.platform.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.client.model.BedrockGunModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.admany.taczt.platform.fabric.client.GunRenderProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {BedrockGunModel.class}
)
public abstract class BedrockGunModelProfileMixin {
   @Unique
   private long taczt$modelStart;

   @Inject(
      method = {"render"},
      at = {@At("HEAD")},
      require = 1
   )
   private void taczt$beginModel(PoseStack poseStack, ItemStack stack, ItemDisplayContext context, RenderType renderType, int light, int overlay, CallbackInfo callback) {
      this.taczt$modelStart = GunRenderProfiler.begin();
   }

   @Inject(
      method = {"render"},
      at = {@At("RETURN")},
      require = 1
   )
   private void taczt$endModel(PoseStack poseStack, ItemStack stack, ItemDisplayContext context, RenderType renderType, int light, int overlay, CallbackInfo callback) {
      GunRenderProfiler.endModel(this.taczt$modelStart);
   }
}
