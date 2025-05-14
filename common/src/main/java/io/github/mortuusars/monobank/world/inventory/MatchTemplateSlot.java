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

    public ItemStack getTemplate() {
        return template;
    }

    public Component getTemplateTooltip() {
        if (templateTooltip == null) {
            templateTooltip = TextObfuscator.obfuscate(template.getHoverName().getString(999),
                    Config.Server.COMBINATION_OBFUSCATION.get());
        }
        return templateTooltip;
    }
}
