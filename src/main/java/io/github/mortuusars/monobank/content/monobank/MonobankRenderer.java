package io.github.mortuusars.monobank.content.monobank;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Vector3f;
import io.github.mortuusars.monobank.Monobank;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders Monobank animated door and stored items inside.
 */
public class MonobankRenderer <T extends BlockEntity & LidBlockEntity> implements BlockEntityRenderer<T> {

    public static final ResourceLocation DOOR_MODEL_LOCATION = Monobank.resource("block/monobank_door");

    public MonobankRenderer(BlockEntityRendererProvider.Context context) { }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!(blockEntity instanceof MonobankBlockEntity monobankEntity))
            return;

        float pixel = 1f / 16f;
        BlockState blockState = blockEntity.getBlockState();

        // Rotate in facing direction:
        float facingYRotation = blockState.getValue(ChestBlock.FACING).getOpposite().toYRot();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-facingYRotation));
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        // Rotate door around the hinge:
        poseStack.pushPose();
        poseStack.translate(pixel * 2, 0d, 0d); // Shift X by 2 pixels to place at the center.

        float openness = blockEntity.getOpenNess(partialTick); // Get how much door is open. From 0 to 1.
        openness = openness < 0.5 ? 4 * openness * openness * openness : (float) (1 - Math.pow(-2 * openness + 2, 3) / 2); // CubicInOut easing:
        float opennessRotation = openness * 112.5f; // 112.5 is the max door opening rotation degrees.
        poseStack.mulPose(Vector3f.YP.rotationDegrees(opennessRotation));

        // Idk what this does (ChestRenderer has it) :
        int i = 0xFFFFFF;
        float f = (float)(i >> 16 & 255) / 255.0F;
        float f1 = (float)(i >> 8 & 255) / 255.0F;
        float f2 = (float)(i & 255) / 255.0F;

        // Door does not get the block light value - so we set it manually here, to darken it a little.
        int light = 0x900000;

        BakedModel model = Minecraft.getInstance().getModelManager().getModel(DOOR_MODEL_LOCATION);

        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(poseStack.last(),
                bufferSource.getBuffer(RenderType.solid()), null, model, f, f1, f2, light, packedOverlay, EmptyModelData.INSTANCE);

        poseStack.popPose();

        // TODO: Render stored contents:
//        if (openness > 0.0f) { // Do not render contents if door is closed.
//            ItemStack storedItemStack = monobankEntity.getStoredItemStack();
//            if (!storedItemStack.isEmpty())
//                renderContents(storedItemStack, monobankEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
//        }
    }

    public void renderContents(ItemStack stack, BlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        float pixel = 1f / 16f;
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        boolean isBlockItem = itemRenderer.getModel(stack, entity.getLevel(), null, 0)
                .isGui3d();

        int maxCapacityCount = 8;

        int count = (int) (Math.min(stack.getCount() / (float)Monobank.getSlotCapacity(), 1.0f) * maxCapacityCount);
        count = Math.max(count, 1); // Should render at least 1

//        for (int i = 0; i < maxCapacityCount; i++) {
//
//        }


        if (isBlockItem) {
            poseStack.pushPose();

            float scale = 0.3f;
            float margin = 0.1f;
//            poseStack.translate(0.5f, scale / 2 + (pixel * 2), 0.5f);
                        poseStack.translate(0.0f, scale / 2 + (pixel * 2), 0.0f);
            poseStack.scale(scale, scale, scale);

//            int levels = count > 4 ? 2 : 1;


            Grid firstLevelGrid = new Grid(2, 2);
            Grid secondLevelGrid = new Grid(2, 2);

            for (int i = 0; i < count; i++) {
                if (count <= 4)
                    firstLevelGrid.addElement();
                else
                    secondLevelGrid.addElement();
            }

            List<Vector3f> positions = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                positions.add(Vector3f.ZERO.copy());
            }

//            NonNullList<Vector3f> positions = NonNullList.withSize(count, Vector3f.ZERO);
            int elementIndex = 0;

            int rows = firstLevelGrid.getRowsCount();
            float height = (scale + margin) * rows;

            for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
                int rowSize = firstLevelGrid.getRowSize(rowIndex);
                float rowWidth = (scale + margin) * rowSize;

                float xStart = 1.0f - rowWidth;
                float zStart = 1.0f - height;

                for (int element = 0; element < rowSize; element++) {
                    positions.get(elementIndex).add(xStart + rowWidth / rowSize, 0f, zStart + height / rows * (rowIndex + 1));
                    elementIndex++;
                }
            }

            int secondRows = secondLevelGrid.getRowsCount();
            float secondHeight = (scale + margin) * secondRows;

            for (int rowIndex = 0; rowIndex < secondRows; rowIndex++) {
                int rowSize = secondLevelGrid.getRowSize(rowIndex);
                float rowWidth = (scale + margin) * rowSize;

                float xStart = 1.0f - rowWidth;
                float zStart = 1.0f - secondHeight;

                for (int element = 0; element < rowSize; element++) {
                    positions.get(elementIndex).add(xStart + rowWidth / rowSize, scale, zStart + secondHeight / secondRows * (rowIndex + 1));
                    elementIndex++;
                }
            }


            for (Vector3f pos : positions) {
                poseStack.pushPose();
                poseStack.translate(pos.x(), pos.y(), pos.z());
                itemRenderer.renderStatic(stack, ItemTransforms.TransformType.NONE, 0xCC0000,
                        packedOverlay, poseStack, bufferSource, (int)entity.getBlockPos().asLong());
                poseStack.popPose();
            }

            poseStack.popPose();
        }

    }

    public static class Grid {
        public static class Line {
            private int elements, maxElements;

            public Line(int maxElements) {
                this.maxElements = maxElements;
            }

            public boolean addElement() {
                if (elements == maxElements)
                    return false;

                elements++;
                return true;
            }
            public boolean hasSpace() {
                return elements < maxElements;
            }
            public int getElementCount() {
                return elements;
            }
        }

        private List<Line> grid;
        public Grid(int rows, int columns) {
            grid = new ArrayList(rows);
            for (int i = 0; i < rows; i++) {
                grid.add(new Line(columns));
            }
        }

        public boolean addElement() {
            for (Line line : grid) {
                if (line.hasSpace()) {
                    line.addElement();
                    return true;
                }
            }
            return false;
        }

        public int getRowSize(int rowIndex) {
            if (rowIndex > grid.size())
                return -1;
            return grid.get(rowIndex).getElementCount();
        }

        public int getRowsCount() {
            int count = 0;
            for (Line line : grid) {
                if (line.getElementCount() > 0)
                    count++;
            }
            return count;
        }

        public int getColumnCount() {
            int count = 0;
            for (Line line : grid) {
                count = Math.max(count, line.getElementCount());
            }
            return count;
        }
    }
}
