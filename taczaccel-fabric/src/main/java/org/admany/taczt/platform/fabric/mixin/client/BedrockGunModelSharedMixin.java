package org.admany.taczt.platform.fabric.mixin.client;

import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.client.model.BedrockGunModel;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import org.admany.taczt.platform.fabric.client.BulletRenderLookupCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(
   value = {BedrockGunModel.class}
)
public abstract class BedrockGunModelSharedMixin {
   private static final AttachmentType[] TACZT$ATTACHMENT_TYPES = AttachmentType.values();

   @Redirect(
      method = {"render", "renderAccelerated"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/tacz/guns/api/item/attachment/AttachmentType;values()[Lcom/tacz/guns/api/item/attachment/AttachmentType;"
),
      require = 0
   )
   private AttachmentType[] taczt$reuseAttachmentTypes() {
      return TACZT$ATTACHMENT_TYPES;
   }

   @Redirect(
      method = {"render", "renderAccelerated"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/tacz/guns/api/TimelessAPI;getClientAttachmentIndex(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;"
),
      require = 0
   )
   private Optional<ClientAttachmentIndex> taczt$reuseAttachmentIndex(ResourceLocation attachmentId) {
      return BulletRenderLookupCache.getAttachmentIndex(attachmentId);
   }
}
