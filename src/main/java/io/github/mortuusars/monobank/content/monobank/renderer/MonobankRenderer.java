package io.github.mortuusars.monobank.content.monobank.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.content.monobank.MonobankBlock;
import io.github.mortuusars.monobank.content.monobank.MonobankBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
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

    protected BlockContentsRenderer blocksRenderer;
    protected ItemContentsRenderer itemsRenderer;

    public MonobankRenderer(BlockEntityRendererProvider.Context ignoredContext) {
        blocksRenderer = new BlockContentsRenderer();
        itemsRenderer = new ItemContentsRenderer();
    }

    @SuppressWarnings("NullableProblems")
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


        BakedModel model = Minecraft.getInstance().getModelManager().getModel(DOOR_MODEL_LOCATION);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(poseStack.last(),
                bufferSource.getBuffer(RenderType.solid()), null, model, 1f, 1f, 1f, packedLight, packedOverlay, EmptyModelData.INSTANCE);

        // Not rendering with AO because shading is broken when there is adjacent solid blocks. Shade is too dark.
//        Minecraft.getInstance().getBlockRenderer().getModelRenderer().tesselateWithAO(monobankEntity.getLevel(), model,
//                monobankEntity.getBlockState(), monobankEntity.getBlockPos().relative(monobankEntity.getBlockState().getValue(MonobankBlock.FACING)),
//                poseStack, bufferSource.getBuffer(RenderType.cutout()), true,
//                monobankEntity.getLevel().getRandom(), 0, packedOverlay, EmptyModelData.INSTANCE);

        poseStack.popPose();

        // Render insides:
        if (openness > 0.0f) { // Do not render if door is closed.
            ItemStack storedItemStack = monobankEntity.getStoredItemStack();
            if (!storedItemStack.isEmpty())
                renderContents(storedItemStack, monobankEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    public void renderContents(ItemStack stack, MonobankBlockEntity monobankEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        boolean renderedAsBlock = Minecraft.getInstance()
                .getItemRenderer()
                        .getModel(stack, monobankEntity.getLevel(), null, 0)
                        .isGui3d();

        float fullness = monobankEntity.getFullness();

        if (renderedAsBlock)
            blocksRenderer.render(stack, fullness, monobankEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        else {
            itemsRenderer.render(stack, fullness, monobankEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }
}
