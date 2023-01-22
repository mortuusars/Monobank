package io.github.mortuusars.monobank.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class Configuration {
    private static final ForgeConfigSpec COMMON_CONFIG_SPEC;

    // Monobank:
    public static final ForgeConfigSpec.IntValue MONOBANK_CAPACITY;

    // Lock:
    public static final ForgeConfigSpec.BooleanValue CAN_REPLACE_OTHER_PLAYERS_LOCKS;

    // Thief:
    public static final ForgeConfigSpec.BooleanValue THIEF_ENABLED;
    public static final ForgeConfigSpec.IntValue THIEF_EFFECT_BASE_DURATION;
    public static final ForgeConfigSpec.BooleanValue THIEF_INCLUDE_OTHER_CONTAINERS;
    public static final ForgeConfigSpec.BooleanValue THIEF_OPENING_PLAYER_OWNED_IS_A_CRIME;

    // Structures:
    public static final ForgeConfigSpec.BooleanValue GENERATE_VILLAGE_STRUCTURES;
    public static final ForgeConfigSpec.IntValue VAULT_WEIGHT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        // --------

        builder.push("Monobank");

        MONOBANK_CAPACITY = builder
                .comment("Maximum amount of items that can be stored in Monobank.")
                .defineInRange("Capacity", 8192, 1, Integer.MAX_VALUE);

        builder.pop();

        // --------

        builder.push("Lock");

        CAN_REPLACE_OTHER_PLAYERS_LOCKS = builder
                .comment("If enabled - players will be able to change locks in other player's banks.")
                .define("CanReplaceOtherPlayersLocks", false);

        builder.pop();

        // --------

        builder.push("Thief");

        THIEF_ENABLED = builder
                .comment("Player will be marked as Thief when stealing from a village.")
                .define("ThiefEnabled", true);

        THIEF_EFFECT_BASE_DURATION = builder
                .comment("Base duration (in seconds) of a 'Thief' debuff.")
                .defineInRange("ThiefDurationSeconds", 360, 1, Integer.MAX_VALUE);

        THIEF_OPENING_PLAYER_OWNED_IS_A_CRIME = builder
                .comment("Opening, unlocking or breaking player-owned Monobank is considered a crime (same as the npc-owned ones).")
                .define("OpeningPlayerOwnedBankCountsAsTheft", false);

        THIEF_INCLUDE_OTHER_CONTAINERS = builder
                .comment("Opening or breaking containers (chests, barrels, etc..) is also counts as a crime.",
                        "Player is marked as Thief only when container has a LootTable and has not been opened before. (Only on first open)")
                .define("StealingFromContainersIsACrime", true);

        builder.pop();

        // -------

        builder.push("StructureGeneration");

        GENERATE_VILLAGE_STRUCTURES = builder
                .comment("Vault buildings will generate in villages.")
                .define("GenerateVaults", true);

        VAULT_WEIGHT = builder
                .comment("Vault building generation weight. Larger number = more chances to generate.")
                .defineInRange("VaultWeight", 15, 1, Integer.MAX_VALUE);

        builder.pop();

        // --------

        COMMON_CONFIG_SPEC = builder.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG_SPEC);
    }
}
