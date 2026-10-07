package org.admany.taczt.platform.fabric.mixin;

import com.tacz.guns.util.block.BlockRayTrace;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.admany.taczt.platform.fabric.raytrace.PassThroughBlockCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(
   value = {BlockRayTrace.class}
)
public abstract class BlockRayTraceMixin {
   @Redirect(
      method = {"lambda$rayTraceBlocks$1"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/resources/ResourceLocation;toString()Ljava/lang/String;"
),
      require = 1
   )
   private static String taczt$capturePassThroughBlockId(ResourceLocation id) {
      return PassThroughBlockCache.capture(id);
   }

   @Redirect(
      method = {"lambda$rayTraceBlocks$1"},
      at = @At(
   value = "INVOKE",
   target = "Ljava/util/List;contains(Ljava/lang/Object;)Z"
),
      require = 1
   )
   private static boolean taczt$containsPassThroughBlock(List<String> configuredIds, Object originalArgument) {
      return PassThroughBlockCache.contains(configuredIds, originalArgument);
   }
}
