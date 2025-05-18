package io.github.mortuusars.monobank.client.gui.rendering;

import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Contains modified copies of GuiGraphics methods with the ability to render slots bigger than 16px.
 */
public class ExtendedSlotRenderer {
    public static void renderFakeItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y,
                                      int seed, int zOffset, int packedLight, float width, float height) {
        renderItem(guiGraphics, null, Minecraft.getInstance().level, stack, x, y, seed, zOffset, packedLight, width, height);
    }

    public static void renderFakeItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y,
                                  int seed, int zOffset, float width, float height) {
        renderItem(guiGraphics, null, Minecraft.getInstance().level, stack, x, y, seed, zOffset, LightTexture.FULL_BRIGHT, width, height);
    }

    public static void renderItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y,
                                  int seed, int zOffset, float width, float height) {
        renderItem(guiGraphics, Minecraft.getInstance().player, Minecraft.getInstance().level, stack, x, y, seed, zOffset, LightTexture.FULL_BRIGHT, width, height);
    }

    public static void renderItem(GuiGraphics guiGraphics, @Nullable LivingEntity entity, @Nullable Level level,
                                  ItemStack stack, int x, int y, int seed, int zOffset, int packedLight, float width, float height) {
        if (stack.isEmpty()) return;

        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, level, entity, seed);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x + width / 2, y + height / 2, (float)(150 + (model.isGui3d() ? zOffset : 0)));

        try {
            guiGraphics.pose().scale(width, -height, width);
            boolean useFlatLighting = !model.usesBlockLight();
            if (useFlatLighting) {
                Lighting.setupForFlatItems();
            }

            Minecraft.getInstance().getItemRenderer().render(stack, ItemDisplayContext.GUI, false,
                    guiGraphics.pose(), guiGraphics.bufferSource(), packedLight, OverlayTexture.NO_OVERLAY, model);

            guiGraphics.flush();

            if (useFlatLighting) {
                Lighting.setupFor3DItems();
            }
        } catch (Throwable var12) {
            CrashReport crashreport = CrashReport.forThrowable(var12, "Rendering item");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
            crashreportcategory.setDetail("Item Type", () -> String.valueOf(stack.getItem()));
            crashreportcategory.setDetail("Item Components", () -> String.valueOf(stack.getComponents()));
            crashreportcategory.setDetail("Item Foil", () -> String.valueOf(stack.hasFoil()));
            throw new ReportedException(crashreport);
        }

        guiGraphics.pose().popPose();
    }

    public static void renderItemDecorations(GuiGraphics guiGraphics, Font font, ItemStack stack, int x, int y, @Nullable String count, int width, int height) {
        if (stack.isEmpty()) return;

        renderDurabilityBar(guiGraphics, stack, x, y, width, height);
        renderCount(guiGraphics, font, stack, x, y, count, width, height);
        renderCooldownOverlay(guiGraphics, stack, x, y, width, height);
    }

    private static void renderDurabilityBar(GuiGraphics guiGraphics, ItemStack stack, int x, int y, int width, int height) {
        if (!stack.isBarVisible()) return;

        int leftRightPadding = (int) (2 / 16.0f * width);
        int bottomPadding = (int) (3 / 16.0f * height);

        int filledBarWidth = (int) (stack.getBarWidth() / 16f * width);
        int barColor = stack.getBarColor();

        int barStartX = x + leftRightPadding;
        int barStartY = y + height - bottomPadding;

        int barThickness = Math.max(2, (int) (2 / 16.0f * height));
        int filledBarThickness = Math.max(1, (int) (1 / 16.0f * height));

        guiGraphics.fill(RenderType.guiOverlay(), barStartX, barStartY, barStartX + filledBarWidth, barStartY + barThickness, 100, 0xFF000000);
        guiGraphics.fill(RenderType.guiOverlay(), barStartX, barStartY, barStartX + filledBarWidth, barStartY + filledBarThickness, 110, barColor | 0xFF000000);
    }

    public static void renderCount(GuiGraphics guiGraphics, Font font, ItemStack stack, int x, int y, @Nullable String count, int width, int height) {
        if (stack.getCount() <= 1 && count == null) return;

        String countStr = count == null ? String.valueOf(stack.getCount()) : count;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0D, 0.0D, 200.0F);

        float startX = x + width + (1 / 16f * width) - font.width(countStr);
        float startY = y + height + (2 / 16f * height) - font.lineHeight;

        guiGraphics.drawString(font, countStr, (int)startX, (int)startY, 0xFFFFFFFF, true);
        guiGraphics.pose().popPose();
    }

    public static void renderCooldownOverlay(GuiGraphics guiGraphics, ItemStack stack, int x, int y, int width, int height) {
        LocalPlayer player = Minecraft.getInstance().player;
        float cooldownPercent = player == null ? 0.0F : player.getCooldowns().getCooldownPercent(stack.getItem(), Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true));
        if (cooldownPercent > 0.0F) {
            guiGraphics.fill(RenderType.guiOverlay(), x, y + Mth.floor(height * (1.0F - cooldownPercent)), width, Mth.ceil(height * cooldownPercent), 0x7FFFFFFF);
        }
    }
}
