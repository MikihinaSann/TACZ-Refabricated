package org.admany.taczt.platform.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.client.model.BedrockAttachmentModel;
import com.tacz.guns.client.model.functional.AttachmentRender;
import com.tacz.guns.client.renderer.item.AttachmentItemRenderer;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import com.tacz.guns.util.RenderDistance;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.admany.taczt.platform.fabric.client.AttachmentPoseStackPool;
import org.admany.taczt.platform.fabric.client.BulletRenderLookupCache;
import org.admany.taczt.platform.fabric.client.GunRenderProfiler;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {AttachmentRender.class}
)
public abstract class AttachmentRenderMixin {
   @Unique
   private static long taczt$attachmentDeferredStarted;

   @Overwrite
   public static void renderAttachment(ItemStack attachment, ItemStack gun, PoseStack poseStack, ItemDisplayContext context, int light, int overlay) {
      poseStack.translate((double)0.0F, (double)-1.5F, (double)0.0F);
      Item var7 = attachment.getItem();
      if (var7 instanceof IAttachment attachmentItem) {
         ResourceLocation attachmentId = attachmentItem.getAttachmentId(attachment);
         Optional<ClientAttachmentIndex> index = BulletRenderLookupCache.getAttachmentIndex(attachmentId);
         if (index.isPresent()) {
            ClientAttachmentIndex clientIndex = (ClientAttachmentIndex)index.get();
            BedrockAttachmentModel model = clientIndex.getAttachmentModel();
            ResourceLocation texture = clientIndex.getModelTexture();
            if (model != null && texture != null) {
               Pair<BedrockAttachmentModel, ResourceLocation> lodModel = clientIndex.getLodModel();
               if (lodModel != null && !RenderDistance.inRenderHighPolyModelDistance(poseStack) && !context.firstPerson()) {
                  model = (BedrockAttachmentModel)lodModel.getLeft();
                  texture = (ResourceLocation)lodModel.getRight();
               }

               long started = GunRenderProfiler.begin();
               model.render(attachment, gun, poseStack, context, RenderType.entityCutout(texture), light, overlay);
               GunRenderProfiler.endAttachmentModel(started);
            }
         } else {
            MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer consumer = source.getBuffer(RenderType.entityTranslucent(MissingTextureAtlasSprite.getLocation()));
            long started = GunRenderProfiler.begin();
            AttachmentItemRenderer.SLOT_ATTACHMENT_MODEL.renderToBuffer(poseStack, consumer, light, overlay);
            GunRenderProfiler.endAttachmentFallback(started);
         }
      }
   }

   @Inject(
      method = {"lambda$render$2"},
      at = {@At("HEAD")},
      require = 1
   )
   private void taczt$beginAttachmentPoseScope(CallbackInfo callback) {
      taczt$attachmentDeferredStarted = GunRenderProfiler.begin();
      AttachmentPoseStackPool.enter();
   }

   @Inject(
      method = {"lambda$render$2"},
      at = {@At("RETURN")},
      require = 1
   )
   private void taczt$endAttachmentPoseScope(CallbackInfo callback) {
      AttachmentPoseStackPool.leave();
      GunRenderProfiler.endAttachmentDeferred(taczt$attachmentDeferredStarted);
   }

   @Redirect(
      method = {"lambda$render$2"},
      at = @At(
   value = "NEW",
   target = "Lcom/mojang/blaze3d/vertex/PoseStack;"
),
      require = 1
   )
   private PoseStack taczt$borrowAttachmentPoseStack() {
      return AttachmentPoseStackPool.borrow();
   }
}
