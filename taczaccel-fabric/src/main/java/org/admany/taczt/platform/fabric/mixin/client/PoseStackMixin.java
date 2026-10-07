package org.admany.taczt.platform.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Deque;
import org.admany.taczt.platform.fabric.client.BedrockPoseStackPool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PoseStack.class)
public abstract class PoseStackMixin {
    @Redirect(
            method = "popPose",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Deque;removeLast()Ljava/lang/Object;"
            ),
            require = 1
    )
    private Object taczt$releasePose(Deque<?> stack) {
        Object removed = stack.removeLast();
        if (BedrockPoseStackPool.isActive() && removed instanceof PoseStack.Pose pose) {
            BedrockPoseStackPool.release(pose);
        }
        return removed;
    }
}
