package org.admany.taczt.platform.fabric.mixin;

import com.tacz.guns.resource.manager.LazyJsonDataManager;
import java.io.BufferedReader;
import java.io.IOException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.admany.taczt.core.cache.CompressedResourceCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {LazyJsonDataManager.class}
)
public abstract class LazyJsonDataManagerMixin {
   @Inject(
      method = {"prepare"},
      at = {@At("HEAD")},
      require = 1
   )
   private void taczt$beginResourceGeneration(ResourceManager manager, ProfilerFiller profiler, CallbackInfoReturnable<?> callback) {
      CompressedResourceCache.beginGeneration(this);
   }

   @Inject(
      method = {"readResourceElement"},
      at = {@At("HEAD")},
      require = 1
   )
   private void taczt$beginResourceRead(ResourceManager manager, ResourceLocation resourcePath, CallbackInfoReturnable<?> callback) {
      CompressedResourceCache.beginRead(this, resourcePath);
   }

   @Inject(
      method = {"readResourceElement"},
      at = {@At("RETURN")},
      require = 1
   )
   private void taczt$endResourceRead(ResourceManager manager, ResourceLocation resourcePath, CallbackInfoReturnable<?> callback) {
      CompressedResourceCache.endRead();
   }

   @Redirect(
      method = {"openReader"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/server/packs/resources/Resource;openAsReader()Ljava/io/BufferedReader;",
   remap = true
),
      require = 1,
      remap = true
   )
   private BufferedReader taczt$openCachedReader(Resource resource) throws IOException {
      return CompressedResourceCache.openReader(resource);
   }
}
