package org.admany.taczt.platform.fabric.mixin.client;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import com.tacz.guns.client.animation.statemachine.ItemAnimationStateContext;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import java.util.Optional;
import java.util.function.BiFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {GunAnimationStateContext.class}
)
public abstract class GunAnimationStateContextMixin extends ItemAnimationStateContext {
   @Shadow
   private GunData gunData;

   @Inject(
      method = {"processGunData"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 1,
      remap = false
   )
   private <T> void taczt$emptyWhileGunDataIsUnavailable(BiFunction<IGun, GunDisplayInstance, T> function, CallbackInfoReturnable<Optional<T>> callback) {
      if (this.gunData == null) {
         callback.setReturnValue(Optional.empty());
      }

   }

   @Inject(
      method = {"isOverHeat"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 1,
      remap = false
   )
   private void taczt$guardOverheat(CallbackInfoReturnable<Boolean> callback) {
      if (this.gunData == null) {
         callback.setReturnValue(false);
      }

   }

   @Inject(
      method = {"getHeatProgress"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 1,
      remap = false
   )
   private void taczt$guardHeatProgress(CallbackInfoReturnable<Float> callback) {
      if (this.gunData == null) {
         callback.setReturnValue(0.0F);
      }

   }

   @Inject(
      method = {"getShootInterval"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 1,
      remap = false
   )
   private void taczt$guardShootInterval(CallbackInfoReturnable<Long> callback) {
      if (this.gunData == null) {
         callback.setReturnValue(0L);
      }

   }

   @Inject(
      method = {"shouldSlide"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 1,
      remap = false
   )
   private void taczt$guardSlide(CallbackInfoReturnable<Boolean> callback) {
      if (this.gunData == null) {
         callback.setReturnValue(false);
      }

   }
}
