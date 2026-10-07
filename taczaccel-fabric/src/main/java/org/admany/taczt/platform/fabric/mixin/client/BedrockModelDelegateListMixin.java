package org.admany.taczt.platform.fabric.mixin.client;

import com.tacz.guns.client.model.IFunctionalRenderer;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(
   value = {BedrockModel.class}
)
public abstract class BedrockModelDelegateListMixin {
   private static final String TACZT$FULL_RENDER = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IIFFFF)V";
   @Shadow
   protected List<IFunctionalRenderer> delegateRenderers;

   @Redirect(
      method = {"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IIFFFF)V"},
      at = @At(
   value = "NEW",
   target = "Ljava/util/ArrayList;"
),
      require = 1
   )
   private ArrayList<IFunctionalRenderer> taczt$createReentrantDelegateList() {
      List var2 = this.delegateRenderers;
      if (var2 instanceof ArrayList<?> list) {
         list.clear();
         @SuppressWarnings("unchecked")
         ArrayList<IFunctionalRenderer> renderers = (ArrayList<IFunctionalRenderer>) (ArrayList<?>) list;
         return renderers;
      } else {
         return new ArrayList<>();
      }
   }
}
