package io.github.mortuusars.monobank.client.gui.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.monobank.client.gui.screen.MonobankScreen;
import io.github.mortuusars.monobank.world.block.monobank.component.Combination;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class CombinationTooltip implements ClientTooltipComponent, TooltipComponent {
    private final ArrayList<ItemStack> items;

    public CombinationTooltip(Combination combination) {
        items = new ArrayList<>();
        for (int i = 0; i < Combination.SIZE; i++) {
            items.add(new ItemStack(combination.getItem(i)));
        }
    }

    @Override
    public void renderImage(@NotNull Font font, int mouseX, int mouseY, GuiGraphics graphics) {
        graphics.blit(MonobankScreen.TEXTURE, mouseX, mouseY, 176, 12, 72, 30);
        for (int i = 0; i < items.size(); i++) {
            graphics.renderItem(items.get(i), mouseX + 7 + 18 * i + 3 * i, mouseY + 7);
        }
    }

    @Override
    public int getHeight() {
        return 32;
    }

    @Override
    public int getWidth(@NotNull Font font) {
        return 18 * 3 + 2;
    }
}
