package org.admany.taczt.platform.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import org.admany.taczt.platform.fabric.client.BedrockPoseStackPool;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PoseStack.Pose.class)
public abstract class PoseStackPoseMixin {
    @Redirect(
            method = "<init>(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;)V",
            at = @At(value = "NEW", target = "Lorg/joml/Matrix4f;"),
            require = 1
    )
    private Matrix4f taczt$borrowPoseMatrix(Matrix4fc source) {
        return BedrockPoseStackPool.borrowPoseMatrix(source);
    }

    @Redirect(
            method = "<init>(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;)V",
            at = @At(value = "NEW", target = "Lorg/joml/Matrix3f;"),
            require = 1
    )
    private Matrix3f taczt$borrowNormalMatrix(Matrix3fc source) {
        return BedrockPoseStackPool.borrowNormalMatrix(source);
    }
}
