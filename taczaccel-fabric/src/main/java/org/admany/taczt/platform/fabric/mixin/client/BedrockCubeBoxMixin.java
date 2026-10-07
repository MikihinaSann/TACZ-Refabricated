package org.admany.taczt.platform.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tacz.guns.client.model.bedrock.BedrockCubeBox;
import com.tacz.guns.client.model.bedrock.BedrockPolygon;
import org.admany.taczt.platform.fabric.client.BedrockCubeCompiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(
   value = {BedrockCubeBox.class}
)
public abstract class BedrockCubeBoxMixin {
   @Shadow
   @Final
   private BedrockPolygon[] polygons;
   @Unique
   private BedrockCubeCompiler.CompiledGeometry taczt$geometry;

   @Overwrite
   public void compile(PoseStack.Pose pose, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha) {
      BedrockCubeCompiler.CompiledGeometry geometry = this.taczt$geometry;
      if (geometry == null) {
         geometry = BedrockCubeCompiler.prepare(this.polygons);
         this.taczt$geometry = geometry;
      }

      BedrockCubeCompiler.compile(geometry, pose, consumer, light, overlay, red, green, blue, alpha);
   }
}
