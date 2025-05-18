package io.github.mortuusars.monobank.client.gui.screen;

import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.client.gui.tooltip.CombinationTooltip;
import io.github.mortuusars.monobank.util.NumberFormatter;
import io.github.mortuusars.monobank.world.inventory.BigSlot;
import io.github.mortuusars.monobank.world.block.monobank.MonobankBlockEntity;
import io.github.mortuusars.monobank.world.inventory.ResizeableSlot;
import io.github.mortuusars.monobank.world.inventory.menu.MonobankMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MonobankScreen extends AbstractContainerScreen<MonobankMenu> {
    public static final ResourceLocation TEXTURE = Monobank.resource("textures/gui/monobank.png");

    protected final Component CTRL_TOOLTIP = Component.translatable("monobank.gui.monobank.tooltip.ctrl_take_single")
            .withStyle(ChatFormatting.DARK_GRAY);
    protected final Component CTRL_SHIFT_TOOLTIP = Component.translatable("monobank.gui.monobank.tooltip.ctrl_shift_take_single")
            .withStyle(ChatFormatting.DARK_GRAY);
    protected final Component OWNER_TOOLTIP = Component.translatable("monobank.gui.monobank.tooltip.owner");
    protected final Component BREAK_IN_ATTEMPTED_TOOLTIP = Component.translatable("monobank.gui.monobank.tooltip.break_in_attempted");
    protected final Component BREAK_IN_SUCCEEDED_TOOLTIP = Component.translatable("monobank.gui.monobank.tooltip.break_in_succeeded");

    protected final MonobankBlockEntity blockEntity;
    protected Optional<TooltipComponent> combinationTooltip;

    protected Level level;

    public MonobankScreen(MonobankMenu containerMenu, Inventory playerinventory, Component title) {
        super(containerMenu, playerinventory, title);
        combinationTooltip = Optional.empty();
        blockEntity = containerMenu.getBlockEntity();
        level = Objects.requireNonNull(Minecraft.getInstance().level);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        if (getMenu().extraInfo.isOwner)
            graphics.blit(TEXTURE, leftPos + 161, topPos + 3, 176, 0, 12, 12);

        if (getMenu().extraInfo.hasWarning()) {
            if (getMenu().extraInfo.breakInSucceeded) {
                if (level.getGameTime() % 10 > 5) // Blinking fast
                    graphics.blit(TEXTURE, leftPos + 151, topPos + 38, 188, 0, 10, 10);
            }
            else if (getMenu().extraInfo.breakInAttempted) {
                if (level.getGameTime() % 26 > 12) // Blinking slowly
                    graphics.blit(TEXTURE, leftPos + 151, topPos + 38, 188, 0, 10, 10);
            }
        }
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics graphics, int x, int y) {
        if (this.menu.getCarried().isEmpty() && hoveredSlot != null &&
                hoveredSlot instanceof BigSlot bankSlot && bankSlot.hasItem()) {
            renderBankSlotTooltip(bankSlot.getItem(), graphics, x, y);
        }
        else if (getMenu().extraInfo.isOwner && isHovering(161, 3, 12, 12, x, y)) { // Owner
            if (combinationTooltip.isEmpty() && !blockEntity.getLock().getCombination().isEmpty())
                combinationTooltip = Optional.of(new CombinationTooltip(blockEntity.getLock().getCombination()));
            graphics.renderTooltip(font, List.of(OWNER_TOOLTIP), combinationTooltip, x, y);
        }
        else if (getMenu().extraInfo.hasWarning() && isHovering(151, 38, 10, 10, x, y)) { // Warning
            if (getMenu().extraInfo.breakInSucceeded)
                graphics.renderTooltip(font, BREAK_IN_SUCCEEDED_TOOLTIP, x, y);
            else if (getMenu().extraInfo.breakInAttempted)
                graphics.renderTooltip(font, BREAK_IN_ATTEMPTED_TOOLTIP, x, y);
        }
        else {
            super.renderTooltip(graphics, x, y);
        }
    }

    protected void renderBankSlotTooltip(ItemStack stack, GuiGraphics graphics, int x, int y) {
        List<Component> tooltip = new ArrayList<>(getTooltipFromItem(Minecraft.getInstance(), stack));
        int stackCount = stack.getCount();
        if (stackCount > 999) {
            String formattedCount = NumberFormatter.separateThousands(stack.getCount());
            MutableComponent newTitle = tooltip.getFirst().copy()
                    .append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))
                    .append(Component.translatable("monobank.gui.monobank.count", formattedCount).withStyle(ChatFormatting.GRAY));
            tooltip.set(0, newTitle);
        }

        tooltip.add(CTRL_TOOLTIP);
        tooltip.add(CTRL_SHIFT_TOOLTIP);

        graphics.renderTooltip(font, tooltip, stack.getTooltipImage(), x, y);
    }

    @Override
    protected void slotClicked(@NotNull Slot slot, int slotId, int mouseButton, @NotNull ClickType type) {
        if (slotId == MonobankMenu.MONOBANK_SLOT_INDEX && mouseButton == 0) {
            if (type == ClickType.PICKUP_ALL) {
                return;
            }

            if (Screen.hasControlDown() && Screen.hasShiftDown()) {
                mouseButton = MonobankMenu.TRANSFER_ALL_BUTTON_ID;
                type = ClickType.QUICK_MOVE;
            }
            else if (Screen.hasControlDown()) {
                mouseButton = MonobankMenu.TRANSFER_SINGLE_BUTTON_ID;
                type = ClickType.QUICK_MOVE;
            }
        }

        super.slotClicked(slot, slotId, mouseButton, type);
    }

    /**
     * Takes resizable slots into account.
     */
    @Override
    public boolean isHovering(Slot slot, double mouseX, double mouseY) {
        if (slot instanceof ResizeableSlot resizeableSlot) {
            return isHovering(slot.x, slot.y, resizeableSlot.getWidth(), resizeableSlot.getHeight(), mouseX, mouseY);
        }
        return super.isHovering(slot, mouseX, mouseY);
    }
}
