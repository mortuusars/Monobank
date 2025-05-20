package io.github.mortuusars.monobank.world.inventory;

import io.github.mortuusars.monobank.Config;
import io.github.mortuusars.monobank.util.TextObfuscator;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class MatchTemplateSlot extends Slot {
    protected final ItemStack template;
    protected @Nullable Component templateTooltip;

    public MatchTemplateSlot(Container container, int index, int x, int y, ItemStack template) {
        super(container, index, x, y);
        this.template = template;
    }

    public ItemStack getTemplateItem() {
        return template;
    }

    public Component getTemplateTooltip() {
        if (templateTooltip == null) {
            createTooltip();
        }
        return templateTooltip;
    }

    public boolean containedItemMatches() {
        return getTemplateItem().getItem().equals(getItem().getItem());
    }

    protected void createTooltip() {
        if (getTemplateItem().isEmpty()) {
            templateTooltip = Component.translatable("monobank.gui.empty");
            return;
        }

        double obfuscationFactor = Config.Server.COMBINATION_HINT_TOOLTIP_OBFUSCATION.get();
        if (obfuscationFactor > 0) {
            String text = template.getHoverName().getString(999);
            templateTooltip = TextObfuscator.obfuscate(text, obfuscationFactor, text.hashCode() + index);
            return;
        }

        templateTooltip = Component.literal(template.getHoverName().getString(999));
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
