package io.github.mortuusars.monobank.content.monobank;

import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.client.gui.component.CombinationTooltip;
import io.github.mortuusars.monobank.client.gui.screen.PatchedAbstractContainerScreen;
import io.github.mortuusars.monobank.core.inventory.BigItemHandlerSlot;
import io.github.mortuusars.monobank.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Optional;

public class MonobankScreen extends PatchedAbstractContainerScreen<MonobankMenu> {
    public static final ResourceLocation TEXTURE = Monobank.resource("textures/gui/monobank.png");

    private final Component CTRL_TOOLTIP = TextUtil.translate("gui.monobank.tooltip.ctrl_take_single").withStyle(ChatFormatting.DARK_GRAY);
    private final Component CTRL_SHIFT_TOOLTIP = TextUtil.translate("gui.monobank.tooltip.ctrl_shift_take_single").withStyle(ChatFormatting.DARK_GRAY);
    private final Component OWNER_TOOLTIP = TextUtil.translate("gui.monobank.tooltip.owner");
    private final Component BREAK_IN_ATTEMPTED_TOOLTIP = TextUtil.translate("gui.monobank.tooltip.break_in_attempted");
    private final Component BREAK_IN_SUCCEEDED_TOOLTIP = TextUtil.translate("gui.monobank.tooltip.break_in_succeeded");

    private final MonobankBlockEntity blockEntity;
    private Optional<TooltipComponent> combinationTooltip;

    public MonobankScreen(MonobankMenu containerMenu, Inventory playerinventory, Component title) {
        super(containerMenu, playerinventory, title);
        combinationTooltip = Optional.empty();
        blockEntity = containerMenu.getBlockEntity();
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void slotClicked(@NotNull Slot pSlot, int pSlotId, int pMouseButton, @NotNull ClickType pType) {

        if (pSlotId == MonobankMenu.MONOBANK_SLOT_INDEX && pMouseButton == 0) {

            if (pType == ClickType.PICKUP_ALL)
                return;

            if (Screen.hasControlDown() && Screen.hasShiftDown()) {
                pMouseButton = -102;
                pType = ClickType.QUICK_MOVE;
            }
            else if (Screen.hasControlDown()) {
                pMouseButton = -101;
                pType = ClickType.QUICK_MOVE;
            }
        }

        super.slotClicked(pSlot, pSlotId, pMouseButton, pType);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        if (getMenu().extraInfo.isOwner)
            graphics.blit(TEXTURE, getGuiLeft() + 161, getGuiTop() + 3, 176, 0, 12, 12);

        if (getMenu().extraInfo.hasWarning()) {
            if (getMenu().extraInfo.breakInSucceeded) {
                if (minecraft.level.getGameTime() % 10 > 5) // Blinking fast
                    graphics.blit(TEXTURE, getGuiLeft() + 151, getGuiTop() + 38, 188, 0, 10, 10);
            }
            else if (getMenu().extraInfo.breakInAttempted) {
                if (minecraft.level.getGameTime() % 26 > 12) // Blinking slowly
                    graphics.blit(TEXTURE, getGuiLeft() + 151, getGuiTop() + 38, 188, 0, 10, 10);
            }
        }
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics graphics, int x, int y) {
        if (this.menu.getCarried().isEmpty() && hoveredSlot != null &&
                hoveredSlot instanceof BigItemHandlerSlot bankSlot && bankSlot.hasItem()) {
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
        else
            super.renderTooltip(graphics, x, y);
    }

    @Override
    protected String getCountStringForSlot(Slot slot, ItemStack itemStack, @Nullable String countString) {
        if (slot instanceof BigItemHandlerSlot && !itemStack.isEmpty())
            return TextUtil.shortenNumber(itemStack.getCount());
        return super.getCountStringForSlot(slot, itemStack, countString);
    }

    protected void renderBankSlotTooltip(ItemStack itemStack, GuiGraphics graphics, int x, int y) {
        List<Component> tooltip = getTooltipFromItem(Minecraft.getInstance(), itemStack);
        int stackCount = itemStack.getCount();
        if (stackCount > 1000) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setGroupingSeparator(' ');
            DecimalFormat numberFormatter = new DecimalFormat("###,###,###", symbols);
            String formattedCount = numberFormatter.format(stackCount);

            MutableComponent newTitle = tooltip.get(0).copy()
                    .append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))
                    .append(TextUtil.translate("gui.monobank.count", formattedCount).withStyle(ChatFormatting.GRAY));
            tooltip.set(0, newTitle);
        }

        tooltip.add(CTRL_TOOLTIP);
        tooltip.add(CTRL_SHIFT_TOOLTIP);

        graphics.renderTooltip(font, tooltip, Optional.empty(), itemStack, x, y);
    }
}
