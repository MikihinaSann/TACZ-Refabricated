package org.admany.taczt.platform.fabric.mixin.client;

import com.tacz.guns.client.gui.overlay.GunHudOverlay;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(
   value = {GunHudOverlay.class}
)
public abstract class GunHudOverlayMixin {
   @Unique
   private static String taczt$version;
   @Unique
   private static String taczt$versionLabel;

   @Redirect(
      method = {"render"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I"
),
      require = 1,
      remap = true
   )
   private int taczt$drawTweaksVersion(GuiGraphics graphics, Font font, String baseVersion, int x, int y, int color) {
      int result = graphics.drawString(font, baseVersion, x, y + 2, color);
      graphics.drawString(font, taczt$versionLabel(), x, y + 14, color);
      return result;
   }

   @Unique
   private static String taczt$version() {
      if (taczt$version == null) {
         taczt$version = FabricLoader.getInstance().getModContainer("taczaccelerated")
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("unknown");
      }

      return taczt$version;
   }

   @Unique
   private static String taczt$versionLabel() {
      if (taczt$versionLabel == null) {
         taczt$versionLabel = "[TaCZ:Accel] " + taczt$version();
      }

      return taczt$versionLabel;
   }
}
