package org.admany.taczt.platform.fabric.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.tacz.guns.client.model.BedrockGunModel;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import com.tacz.guns.util.RenderHelper;
import java.util.Optional;
import java.util.function.Consumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {BedrockGunModel.class}
)
public abstract class BedrockGunModelMixin {
   @Unique
   private boolean taczt$stencilUsed;

   @Inject(
      method = {"render"},
      at = {@At("HEAD")},
      require = 1
   )
   private void taczt$beginRender(CallbackInfo callback) {
      this.taczt$stencilUsed = false;
   }

   @Inject(
      method = {"render"},
      at = {@At("RETURN")},
      require = 1
   )
   private void taczt$endRender(CallbackInfo callback) {
      this.taczt$stencilUsed = false;
   }

   @Redirect(
      method = {"render"},
      at = @At(
   value = "INVOKE",
   target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V",
   ordinal = 1
),
      require = 1
   )
   private void taczt$trackScopeStencil(Optional<ClientAttachmentIndex> index, Consumer<? super ClientAttachmentIndex> consumer) {
      if (index.isPresent() && ((ClientAttachmentIndex)index.get()).isScope()) {
         this.taczt$stencilUsed = true;
      }

      index.ifPresent(consumer);
   }

   @Redirect(
      method = {"render"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/mojang/blaze3d/systems/RenderSystem;stencilOp(III)V"
),
      require = 1
   )
   private void taczt$skipUnusedStencilOperation(int fail, int depthFail, int pass) {
      if (this.taczt$stencilUsed) {
         RenderSystem.stencilOp(fail, depthFail, pass);
      }

   }

   @Redirect(
      method = {"render"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/tacz/guns/util/RenderHelper;disableItemEntityStencilTest()V"
),
      require = 1
   )
   private void taczt$skipUnusedStencilDisable() {
      if (this.taczt$stencilUsed) {
         RenderHelper.disableItemEntityStencilTest();
      }

   }

   @Redirect(
      method = {"render"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearStencil(I)V"
),
      require = 1
   )
   private void taczt$skipUnusedStencilValueClear(int value) {
      if (this.taczt$stencilUsed) {
         RenderSystem.clearStencil(value);
      }

   }

   @Redirect(
      method = {"render"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V"
),
      require = 1
   )
   private void taczt$skipUnusedStencilBufferClear(int mask, boolean osx) {
      if (this.taczt$stencilUsed) {
         RenderSystem.clear(mask, osx);
      }

   }
}
