package io.github.mortuusars.monobank.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class Configuration {
    private static final ForgeConfigSpec COMMON_CONFIG_SPEC;

    public static final ForgeConfigSpec.IntValue MONOBANK_CAPACITY;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("Monobank");

        MONOBANK_CAPACITY = builder
                .comment("Maximum amount of items that can be stored in Monobank.")
                .defineInRange("Capacity", 8192, 1, Integer.MAX_VALUE);

        builder.pop();

        COMMON_CONFIG_SPEC = builder.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG_SPEC);
    }
}
