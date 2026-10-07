package org.admany.taczt.platform.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tacz.guns.client.model.IFunctionalRenderer;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.model.functional.AttachmentRender;
import com.tacz.guns.client.model.functional.LeftHandRender;
import com.tacz.guns.client.model.functional.MuzzleFlashRender;
import com.tacz.guns.client.model.functional.RightHandRender;
import com.tacz.guns.client.model.functional.ShellRender;
import com.tacz.guns.client.model.functional.TextShowRender;
import com.tacz.guns.compat.iris.IrisCompat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.fabricmc.loader.api.FabricLoader;
import org.admany.taczt.platform.fabric.client.GunRenderProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(
   value = {BedrockModel.class}
)
public abstract class BedrockModelMixin {
   private static final String TACZT$FULL_RENDER = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IIFFFF)V";
   @Unique
   private static final boolean TACZT$IRIS_LOADED = FabricLoader.getInstance().isModLoaded("iris");

   @Redirect(
      method = {"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IIFFFF)V"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/tacz/guns/client/model/bedrock/BedrockPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"
),
      require = 1
   )
   private void taczt$profileGeometry(BedrockPart part, PoseStack poseStack, ItemDisplayContext context, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha) {
      long started = GunRenderProfiler.begin();
      part.render(poseStack, context, consumer, light, overlay, red, green, blue, alpha);
      GunRenderProfiler.endModelGeometry(started);
   }

   @Redirect(
      method = {"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IIFFFF)V"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/tacz/guns/compat/iris/IrisCompat;endBatch(Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)Z"
),
      require = 1
   )
   private boolean taczt$profileOculusFlush(MultiBufferSource.BufferSource source) {
      if (!TACZT$IRIS_LOADED) {
         return false;
      } else {
         long started = GunRenderProfiler.begin();
         boolean handled = IrisCompat.endBatch(source);
         if (handled) {
            GunRenderProfiler.endModelFlush(started);
         }

         return handled;
      }
   }

   @Redirect(
      method = {"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IIFFFF)V"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V"
),
      require = 1,
      remap = true
   )
   private void taczt$profileVanillaFlush(MultiBufferSource.BufferSource source, RenderType type) {
      long started = GunRenderProfiler.begin();
      source.endBatch(type);
      GunRenderProfiler.endModelFlush(started);
   }

   @Redirect(
      method = {"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IIFFFF)V"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/tacz/guns/client/model/IFunctionalRenderer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/item/ItemDisplayContext;II)V"
),
      require = 1
   )
   private void taczt$profileDelegates(IFunctionalRenderer renderer, PoseStack poseStack, VertexConsumer consumer, ItemDisplayContext context, int light, int overlay) {
      long started = GunRenderProfiler.begin();
      renderer.render(poseStack, consumer, context, light, overlay);
      GunRenderProfiler.endModelDelegates(started);
      if (renderer instanceof AttachmentRender) {
         GunRenderProfiler.endFunctionalAttachment(started);
      } else if (!(renderer instanceof LeftHandRender) && !(renderer instanceof RightHandRender)) {
         if (renderer instanceof TextShowRender) {
            GunRenderProfiler.endFunctionalText(started);
         } else if (!(renderer instanceof MuzzleFlashRender) && !(renderer instanceof ShellRender)) {
            GunRenderProfiler.endFunctionalOther(started, renderer.getClass().getName());
         } else {
            GunRenderProfiler.endFunctionalEffect(started);
         }
      } else {
         GunRenderProfiler.endFunctionalHand(started);
      }

   }
}
