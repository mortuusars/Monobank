package io.github.mortuusars.monobank.content.monobank;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;

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


        // TODO: Render stored contents:
//        ItemStack storedItemStack = monobankEntity.getStoredItemStack();
//        if (!storedItemStack.isEmpty())
//            renderContents(storedItemStack, monobankEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);


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
        float openNess = blockEntity.getOpenNess(partialTick); // Get how much door is open. From 0 to 1.
        float openNessRotation = openNess * 112.5f; // 112.5 is the max door opening rotation degrees.
        poseStack.mulPose(Vector3f.YP.rotationDegrees(openNessRotation));

        // Idk what this does (Chest :
        int i = 0xFFFFFF;
        float f = (float)(i >> 16 & 255) / 255.0F;
        float f1 = (float)(i >> 8 & 255) / 255.0F;
        float f2 = (float)(i & 255) / 255.0F;

        // Door does not get the block light value - so we set it manually here, to darken it a little.
        // ChestRenderer uses a BrightnessCombiner for this, maybe I need to use it too, if this approach will cause problems.
        int light = 0x900000;

        BakedModel model = Minecraft.getInstance().getModelManager().getModel(DOOR_MODEL_LOCATION);

        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(poseStack.last(),
                bufferSource.getBuffer(RenderType.solid()), null, model, f, f1, f2, light, packedOverlay, EmptyModelData.INSTANCE);

        poseStack.popPose();
    }

    public void renderContents(ItemStack stack, BlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        float pixel = 1f / 16f;
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        boolean isBlockItem = itemRenderer.getModel(stack, entity.getLevel(), null, 0)
                .isGui3d();

        int maxCapacityCount = 8;

//        List<Vector3f> positions = List.of(
//                new Vector3f()
//
//        );

        for (int i = 0; i < maxCapacityCount; i++) {

        }

        poseStack.pushPose();

        if (isBlockItem) {
            float scale = 0.3f;
            poseStack.translate(0.5f, scale / 2 + (pixel * 2), 0.5f);
            poseStack.scale(scale, scale, scale);
        }


        itemRenderer.renderStatic(stack, ItemTransforms.TransformType.NONE, 0xCC0000,
                packedOverlay, poseStack, bufferSource, (int)entity.getBlockPos().asLong());

        poseStack.popPose();
    }
}
