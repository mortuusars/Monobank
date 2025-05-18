package io.github.mortuusars.monobank.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

public class ClientUtil {
    public static String getSneakUseKeyTranslation() {
        Options opt = Minecraft.getInstance().options;
        return opt.keyShift.getTranslatedKeyMessage().getString(999) +
                "+" + opt.keyUse.getTranslatedKeyMessage().getString(999);
    }
}
