package io.github.mortuusars.monobank.content.monobank.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

public class BlockContentsRenderer {
    protected static final int MAX_BLOCKS_COUNT = 8;

    // Hardcoded random rotations. Should have same size as MAX_BLOCKS_COUNT.
    protected List<Integer> yRotations = List.of(-4, 7, -10, 2, 2, -4, 0, 2);

    /**
     * Renders up to 8 blocks in a 2x2 grid depending on the fullness of the bank.
     * Proper rotation to block facing is expected here.
     */
    public void render(ItemStack stack, float fullness, BlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        float pixel = 1f / 16f;
        float scale = 0.3f;
        float margin = 0.28f;
        int count =  Mth.clamp((int)(fullness * MAX_BLOCKS_COUNT), 1, MAX_BLOCKS_COUNT);

        poseStack.pushPose();
        poseStack.translate(0.5f, scale / 2 + (pixel * 2), 0.5f); // Position in the center and on the bank floor.
        poseStack.scale(scale, scale, scale);

        List<Integer> rows = new ArrayList<>();

        // Split by rows of 2:

        for (int i = 0; i < count; i++) {
            rows.add(0);
        }

        int elements = count;
        int row = 0;
        while (elements > 0) {
            if (rows.get(row) != 2) {
                rows.set(row, rows.get(row) + 1);
                elements--;
            }
            else
                row++;
        }

        // Calculate each row's item offsets

        int elementIndex = 0;

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Integer itemsInRow = rows.get(rowIndex);
            if (itemsInRow == 0)
                break;

            float xOffset = itemsInRow == 2 ? scale + margin : 0f;
            float yOffset = rowIndex >= 2 ? 1f : 0;
            float zOffset = rowIndex % 2 == 0 ? scale + margin : -(scale + margin);

            if (rowIndex % 2 == 0 && (rows.size() <= rowIndex + 1 || (rows.size() > rowIndex + 1 && rows.get(rowIndex + 1) == 0)))
                zOffset = 0f;

            for (int item = 0; item < itemsInRow; item++) {
                float itemXOffset = item == 0 ? -xOffset : xOffset;
                poseStack.pushPose();
                poseStack.translate(itemXOffset, yOffset, zOffset);
                poseStack.mulPose(Vector3f.YP.rotationDegrees(yRotations.get(elementIndex)));
                itemRenderer.renderStatic(stack, ItemTransforms.TransformType.NONE, packedLight,
                        packedOverlay, poseStack, bufferSource, (int) entity.getBlockPos().asLong());
                poseStack.popPose();

                elementIndex++;
            }
        }

        poseStack.popPose();
    }
}
