package com.tacz.guns.client.model.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.tacz.guns.client.model.IFunctionalRenderer;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.model.papi.PapiManager;
import com.tacz.guns.client.resource.pojo.display.gun.TextShow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.function.Supplier;

public class TextShowRender implements IFunctionalRenderer {
    private static final long TEXT_EVAL_INTERVAL_MS = 100L;

    private final BedrockModel bedrockModel;
    private final TextShow textShow;
    private final Supplier<ItemStack> gunStackSupplier;
    private String cachedText = "";
    private int cachedXOffset;
    private long lastEvalMillis;

    public TextShowRender(BedrockModel bedrockModel, TextShow textShow, Supplier<ItemStack> gunStackSupplier) {
        this.bedrockModel = bedrockModel;
        this.textShow = textShow;
        this.gunStackSupplier = gunStackSupplier;
    }

    @Override
    public void render(PoseStack poseStack, VertexConsumer vertexBuffer, ItemDisplayContext transformType, int light, int overlay) {
        if (!transformType.firstPerson()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastEvalMillis >= TEXT_EVAL_INTERVAL_MS) {
            lastEvalMillis = now;
            ItemStack gunStack = gunStackSupplier.get();
            String text = gunStack == null ? "" : PapiManager.getTextShow(textShow.getTextKey(), gunStack);
            if (StringUtils.isBlank(text)) {
                text = "";
            }
            if (!text.equals(cachedText)) {
                cachedText = text;
                Font font = Minecraft.getInstance().font;
                int width = font.width(text);
                cachedXOffset = switch (textShow.getAlign()) {
                    case CENTER -> width / 2;
                    case RIGHT -> width;
                    default -> 0;
                };
            }
        }
        if (cachedText.isEmpty()) {
            return;
        }
        poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
        Matrix3f normal = new Matrix3f(poseStack.last().normal());
        Matrix4f pose = new Matrix4f(poseStack.last().pose());

        // 和枪械模型共用顶点缓冲的都需要代理到渲染结束后渲染
        bedrockModel.delegateRender((poseStack1, vertexBuffer1, transformType1, light1, overlay1) -> {
            Font font = Minecraft.getInstance().font;
            boolean shadow = textShow.isShadow();
            int color = textShow.getColorInt();
            float scale = textShow.getScale();
            int packLight = LightTexture.pack(textShow.getTextLight(), textShow.getTextLight());

            PoseStack poseStack2 = new PoseStack();
            poseStack2.last().normal().mul(normal);
            poseStack2.last().pose().mul(pose);
            poseStack2.scale(2 / 300f * scale, -2 / 300f * scale, -2 / 300f);

            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            font.drawInBatch(cachedText, -cachedXOffset, -font.lineHeight / 2f, color, shadow, poseStack2.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, packLight);
        });
    }
}
