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
import java.util.Random;

public class BlockContentsRenderer {
    protected static final int MAX_BLOCKS_COUNT = 8;
    protected List<Vector3f> layout;
    protected List<Integer> yRotations; // Stored random rotations of current layout
    protected int lastCount = -1;

    public void render(ItemStack stack, float fullness, BlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        float pixel = 1f / 16f;
        float scale = 0.3f;
        float margin = 0.28f;
        int count =  Mth.clamp((int)(fullness * MAX_BLOCKS_COUNT), 1, MAX_BLOCKS_COUNT);

        if (count != lastCount)
            recalculateLayout(count, scale, margin);

        poseStack.pushPose();
        poseStack.translate(0.5f, scale / 2 + (pixel * 2), 0.5f); // Position in the center and on the bank floor.
        poseStack.scale(scale, scale, scale);

        for (int index = 0; index < layout.size(); index++) {
            Vector3f offset = layout.get(index);
            poseStack.pushPose();
            poseStack.translate(offset.x(), offset.y(), offset.z());
            poseStack.mulPose(Vector3f.YP.rotationDegrees(yRotations.get(index)));
            itemRenderer.renderStatic(stack, ItemTransforms.TransformType.NONE, 0xCC0000,
                    packedOverlay, poseStack, bufferSource, (int) entity.getBlockPos().asLong());
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private void recalculateLayout(int count, float scale, float margin) {
        layout = new ArrayList<>();
        yRotations = new ArrayList<>();
        Random random = new Random();

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
                layout.add(new Vector3f(itemXOffset, yOffset, zOffset));
                yRotations.add(random.nextInt(-10, 11));
            }
        }

        lastCount = count;
    }
}
