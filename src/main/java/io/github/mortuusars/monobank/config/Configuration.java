package io.github.mortuusars.monobank.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class Configuration {
    private static final ForgeConfigSpec COMMON_CONFIG_SPEC;

    // Monobank:
    public static final ForgeConfigSpec.IntValue MONOBANK_CAPACITY;

    // Ownership:
    public static final ForgeConfigSpec.BooleanValue CAN_RELOCATE_OTHER_PLAYERS_BANK;
    public static final ForgeConfigSpec.BooleanValue CAN_REPLACE_OTHER_PLAYERS_LOCKS;

    // Thief:
    public static final ForgeConfigSpec.BooleanValue THIEF_ENABLED;
    public static final ForgeConfigSpec.IntValue THIEF_EFFECT_BASE_DURATION;
    public static final ForgeConfigSpec.BooleanValue THIEF_INCLUDE_OTHER_CONTAINERS;
    public static final ForgeConfigSpec.BooleanValue THIEF_OPENING_PLAYER_OWNED_IS_A_CRIME;
    public static final ForgeConfigSpec.BooleanValue THIEF_ONLY_NEAR_PROTECTED_STRUCTURES;
    public static final ForgeConfigSpec.IntValue THIEF_PROTECTED_STRUCTURE_RANGE;
    public static final ForgeConfigSpec.BooleanValue THIEF_NO_TRADE;
    public static final ForgeConfigSpec.BooleanValue THIEF_NO_TRADE_ONLY_NEAR_PROTECTED_STRUCTURES;


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

        builder.push("Ownership");

        CAN_RELOCATE_OTHER_PLAYERS_BANK = builder
                .comment("If enabled - players will be able to break other player's banks.", "If disabled - monobank will be indestructible if other player owns it.")
                .define("CanRelocateOtherPlayersBanks", false);

        CAN_REPLACE_OTHER_PLAYERS_LOCKS = builder
                .comment("If enabled - players will be able to change locks in other player's banks.")
                .define("CanReplaceOtherPlayersLocks", false);

        builder.pop();

        // --------

        builder.push("Thief");

        THIEF_ENABLED = builder
                .comment("Player will be marked as Thief if it was seen stealing from a village.",
                        "Entities with tag 'monobank:theft_haters' should be a witness of a theft to mark a player as Thief.")
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

        THIEF_ONLY_NEAR_PROTECTED_STRUCTURES = builder
                .comment("Player is marked as Thief only near 'protected' structures. Defined in a 'monobank:theft_protected' structure tag.",
                        "If disabled - you will be marked as a Thief anywhere in the world, as long as there's a witness of your crime.")
                .define("ThiefOnlyNearProtectedStructures", true);

        THIEF_PROTECTED_STRUCTURE_RANGE = builder
                .comment("Distance to nearest protected structure in blocks that defines 'being near a protected structure'.",
                        "Distance is measured same as in /locate command. Not to the border of a village, but to the center.",
                        "Same as with /locate command structure coordinates will only have x and z values. Y value will be the highest surface point.",
                        "This is fine for villages (they spawn on a surface) but can have an effect if the structure is deep underground.")
                .defineInRange("ProtectedStructureRange", 128, 1, Integer.MAX_VALUE);

        THIEF_NO_TRADE = builder
                .comment("Villagers will refuse to trade with a player marked as Thief.")
                .define("VillagersHateThieves", true);

        THIEF_NO_TRADE_ONLY_NEAR_PROTECTED_STRUCTURES = builder
                .comment("Villagers will check if they are near a protected structure before refusing to trade with a player marked as Thief.",
                        "This setting will have no effect if 'ThiefOnlyNearProtectedStructures' is disabled.")
                .define("VillagersCheckRangeBeforeHatingThieves", false);

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
