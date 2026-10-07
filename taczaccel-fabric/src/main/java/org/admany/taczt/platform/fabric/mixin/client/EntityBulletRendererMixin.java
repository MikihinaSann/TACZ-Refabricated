package org.admany.taczt.platform.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tacz.guns.client.model.BedrockAmmoModel;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.renderer.entity.EntityBulletRenderer;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientAmmoIndex;
import com.tacz.guns.entity.EntityKineticBullet;
import java.util.Optional;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import org.admany.taczt.platform.fabric.client.BulletModelCache;
import org.admany.taczt.platform.fabric.client.BulletRenderLookupCache;
import org.admany.taczt.platform.fabric.client.BulletRenderTypeCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {EntityBulletRenderer.class}
)
public abstract class EntityBulletRendererMixin {
   @Shadow
   public abstract void renderTracerAmmo(EntityKineticBullet var1, float[] var2, float var3, PoseStack var4, int var5);

   @Inject(
      method = {"getModel"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 1,
      remap = false
   )
   private static void taczt$reuseDefaultBulletModel(CallbackInfoReturnable<Optional<BedrockModel>> callback) {
      BedrockModel cached = BulletModelCache.get();
      if (cached != null) {
         callback.setReturnValue(Optional.of(cached));
      }

   }

   @Inject(
      method = {"getModel"},
      at = {@At("RETURN")},
      require = 1,
      remap = false
   )
   private static void taczt$cacheDefaultBulletModel(CallbackInfoReturnable<Optional<BedrockModel>> callback) {
      callback.getReturnValue().ifPresent(BulletModelCache::set);
   }

   @Overwrite
   public void render(EntityKineticBullet bullet, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
      Optional<GunDisplayInstance> display = BulletRenderLookupCache.getGunDisplay(bullet.getGunDisplayId(), bullet.getGunId());
      if (!display.isEmpty()) {
         float[] tracerColor = (float[])bullet.getTracerColorOverride().orElse(((GunDisplayInstance)display.get()).getTracerColor());
         Optional<ClientAmmoIndex> ammoIndex = BulletRenderLookupCache.getAmmoIndex(bullet.getAmmoId());
         if (!ammoIndex.isEmpty()) {
            ClientAmmoIndex index = (ClientAmmoIndex)ammoIndex.get();
            BedrockAmmoModel model = index.getAmmoEntityModel();
            ResourceLocation texture = index.getAmmoEntityTextureLocation();
            if (model != null && texture != null) {
               poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, bullet.yRotO, bullet.getYRot()) - 180.0F));
               poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTick, bullet.xRotO, bullet.getXRot())));
               poseStack.pushPose();
               poseStack.translate((double)0.0F, (double)1.5F, (double)0.0F);
               poseStack.scale(-1.0F, -1.0F, 1.0F);
               model.render(poseStack, ItemDisplayContext.GROUND, RenderType.entityTranslucentCull(texture), light, OverlayTexture.NO_OVERLAY);
               poseStack.popPose();
            }

            if (bullet.isTracerAmmo()) {
               float[] indexTracerColor = index.getTracerColor();
               this.renderTracerAmmo(bullet, indexTracerColor != null ? indexTracerColor : tracerColor, partialTick, poseStack, light);
            }

         }
      }
   }

   @Redirect(
      method = {"lambda$renderTracerAmmo$1"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/renderer/RenderType;energySwirl(Lnet/minecraft/resources/ResourceLocation;FF)Lnet/minecraft/client/renderer/RenderType;"
),
      require = 1,
      remap = true
   )
   private RenderType taczt$reuseTracerRenderType(ResourceLocation texture, float uOffset, float vOffset) {
      return BulletRenderTypeCache.tracerEnergySwirl(texture, uOffset, vOffset);
   }
}
