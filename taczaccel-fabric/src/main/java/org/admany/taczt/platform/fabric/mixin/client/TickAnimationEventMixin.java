package org.admany.taczt.platform.fabric.mixin.client;

import com.tacz.guns.client.event.TickAnimationEvent;
import com.tacz.guns.client.resource.GunDisplayInstance;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import org.admany.taczt.platform.fabric.client.BulletRenderLookupCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(
   value = {TickAnimationEvent.class}
)
public abstract class TickAnimationEventMixin {
   @Redirect(
      method = {"tickAnimation(Lnet/minecraft/client/Minecraft;)V"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/tacz/guns/api/TimelessAPI;getGunDisplay(Lnet/minecraft/world/item/ItemStack;)Ljava/util/Optional;"
),
      require = 1
   )
   private static Optional<GunDisplayInstance> taczt$reuseGunDisplay(ItemStack stack) {
      return BulletRenderLookupCache.getGunDisplay(stack);
   }
}
