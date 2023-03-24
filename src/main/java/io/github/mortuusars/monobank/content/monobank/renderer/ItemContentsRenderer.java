package io.github.mortuusars.monobank.content.monobank.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Vector3f;

import java.util.List;

public class ItemContentsRenderer {

    protected static final int MAX_ITEMS_COUNT = 8;

    protected List<Vector3f> layout = List.of(
            new Vector3f(0.05f, 0.0f, -0.06f),
            new Vector3f(0.28f, 0.36f, -0.11f),
            new Vector3f(-0.05f, 0.40f, -0.01f),
            new Vector3f(0.39f, 0.18f, -0.43f),
            new Vector3f(-0.44f, 0.01f, -0.44f),
            new Vector3f(-0.29f, 0.12f, 0.38f),
            new Vector3f(0.07f, 0.19f, -0.48f),
            new Vector3f(-0.20f, 0.34f, 0.42f));

    /**
     * Renders up to 8 items spread a little from the center.
     * Proper rotation to block facing is expected here.
     */
    public void render(ItemStack stack, float fullness, BlockEntity entity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        float pixel = 1f / 16f;
        float scale = 0.4f;
        int count =  Mth.clamp((int)(fullness * MAX_ITEMS_COUNT), 1, MAX_ITEMS_COUNT);

        poseStack.pushPose();

        poseStack.translate(0.5f, pixel * 2 + scale / 2, 0.5f);
        poseStack.scale(scale, scale, scale);

        for (int i = 0; i < count; i++) {
            Vector3f offset = layout.get(i);
            poseStack.pushPose();
            poseStack.translate(offset.x(), offset.y(), offset.z());
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            itemRenderer.renderStatic(stack, ItemTransforms.TransformType.NONE, packedLight,
                    packedOverlay, poseStack, bufferSource, (int) entity.getBlockPos().asLong());
            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
