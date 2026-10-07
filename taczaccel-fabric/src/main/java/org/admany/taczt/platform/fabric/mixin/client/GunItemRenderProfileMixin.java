package org.admany.taczt.platform.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.client.animation.statemachine.LuaAnimationStateMachine;
import com.tacz.guns.client.event.FirstPersonRenderGunEvent;
import com.tacz.guns.client.model.BedrockGunModel;
import com.tacz.guns.client.renderer.item.GunItemRendererWrapper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.admany.taczt.platform.fabric.client.GunRenderProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {GunItemRendererWrapper.class}
)
public abstract class GunItemRenderProfileMixin {
   @Unique
   private long taczt$firstPersonStart;
   @Unique
   private long taczt$itemStart;

   @Inject(
      method = {"renderFirstPerson"},
      at = {@At("HEAD")},
      require = 1
   )
   private void taczt$beginFirstPerson(LocalPlayer player, ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffers, int light, float partialTick, CallbackInfo callback) {
      this.taczt$firstPersonStart = GunRenderProfiler.begin();
   }

   @Inject(
      method = {"renderFirstPerson"},
      at = {@At("RETURN")},
      require = 1
   )
   private void taczt$endFirstPerson(LocalPlayer player, ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffers, int light, float partialTick, CallbackInfo callback) {
      GunRenderProfiler.endFirstPerson(this.taczt$firstPersonStart);
   }

   @Inject(
      method = {"renderByItem"},
      at = {@At("HEAD")},
      require = 1,
      remap = true
   )
   private void taczt$beginItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay, CallbackInfo callback) {
      this.taczt$itemStart = GunRenderProfiler.begin();
   }

   @Inject(
      method = {"renderByItem"},
      at = {@At("RETURN")},
      require = 1,
      remap = true
   )
   private void taczt$endItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay, CallbackInfo callback) {
      GunRenderProfiler.endItem(this.taczt$itemStart);
   }

   @Redirect(
      method = {"lambda$renderFirstPerson$5"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/tacz/guns/api/client/animation/statemachine/LuaAnimationStateMachine;update()V"
),
      require = 1
   )
   private void taczt$profileAnimationUpdate(LuaAnimationStateMachine<?> machine) {
      long started = GunRenderProfiler.begin();
      machine.update();
      GunRenderProfiler.endAnimation(started);
   }

   @Redirect(
      method = {"lambda$renderFirstPerson$5"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/tacz/guns/client/event/FirstPersonRenderGunEvent;applyFirstPersonGunTransform(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/tacz/guns/client/model/BedrockGunModel;F)V"
),
      require = 1
   )
   private void taczt$profileFirstPersonTransform(LocalPlayer player, ItemStack stack, PoseStack poseStack, BedrockGunModel model, float partialTick) {
      long started = GunRenderProfiler.begin();
      FirstPersonRenderGunEvent.applyFirstPersonGunTransform(player, stack, poseStack, model, partialTick);
      GunRenderProfiler.endTransform(started);
   }
}
