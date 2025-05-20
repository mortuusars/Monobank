package io.github.mortuusars.monobank.config;

import io.github.mortuusars.monobank.integration.thief.ThiefCrime;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class Configuration {
    private static final ForgeConfigSpec COMMON_CONFIG_SPEC;

    // Monobank:
    public static final ForgeConfigSpec.IntValue MONOBANK_CAPACITY;
    public static final ForgeConfigSpec.BooleanValue COMBINATION_ENABLED;

    // Ownership:
    public static final ForgeConfigSpec.BooleanValue OWNER_CAN_UNLOCK_WITHOUT_COMBINATION;
    public static final ForgeConfigSpec.BooleanValue CAN_RELOCATE_OTHER_PLAYERS_BANK;
    public static final ForgeConfigSpec.BooleanValue CAN_REPLACE_OTHER_PLAYERS_LOCKS;

    // Thief:
    public static final ForgeConfigSpec.EnumValue<ThiefCrime> THIEF_CRIME_FOR_UNLOCKING_ATTEMPT;
    public static final ForgeConfigSpec.EnumValue<ThiefCrime> THIEF_CRIME_FOR_UNLOCKING;
    public static final ForgeConfigSpec.EnumValue<ThiefCrime> THIEF_CRIME_FOR_OPENING;

    // Structures:
    public static final ForgeConfigSpec.BooleanValue GENERATE_VILLAGE_STRUCTURES;
    public static final ForgeConfigSpec.IntValue VAULT_WEIGHT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        // --------

        builder.push("Monobank");

        MONOBANK_CAPACITY = builder
                .comment("Maximum amount of items that can be stored in Monobank. Default: 8192")
                .defineInRange("Capacity", 8192, 1, Integer.MAX_VALUE);

        COMBINATION_ENABLED = builder
                .comment("Combination is required to unlock a Monobank. Default: true")
                .define("CombinationEnabled", true);

        builder.pop();

        // --------

        builder.push("Ownership");

        OWNER_CAN_UNLOCK_WITHOUT_COMBINATION = builder
                .comment("Owners can unlock their Monobank without combination. Default: true")
                .define("OwnerCanUnlockWithoutCombination", true);
        
        CAN_RELOCATE_OTHER_PLAYERS_BANK = builder
                .comment("If enabled - players will be able to break other player's banks.", "If disabled - monobank will be indestructible if other player owns it. Default: false")
                .define("CanRelocateOtherPlayersBanks", false);

        CAN_REPLACE_OTHER_PLAYERS_LOCKS = builder
                .comment("If enabled - players will be able to change locks in other player's banks. Default: false")
                .define("CanReplaceOtherPlayersLocks", false);

        builder.pop();

        // --------

        builder.push("Thief");

        THIEF_CRIME_FOR_UNLOCKING_ATTEMPT = builder
                .comment("Crime severity for attempting to unlock a Monobank (opening combination UI).",
                        "Default: LIGHT")
                .defineEnum("crime_severity_for_unlocking_attempt", ThiefCrime.LIGHT);

        THIEF_CRIME_FOR_UNLOCKING = builder
                .comment("Crime severity for unlocking a Monobank.",
                        "Default: MEDIUM")
                .defineEnum("crime_severity_for_unlocking", ThiefCrime.MEDIUM);

        THIEF_CRIME_FOR_OPENING = builder
                .comment("Crime severity for opening a Monobank.",
                        "Default: HEAVY")
                .defineEnum("crime_severity_for_opening", ThiefCrime.HEAVY);

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

    public static class Server {
        public static final ForgeConfigSpec SPEC;

        public static final ForgeConfigSpec.BooleanValue SHOW_HINT_ICON;

        static {
            ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

            SHOW_HINT_ICON = builder
                    .comment("Show item hint icon un unlocking screen. Default: true")
                    .define("ShowHintIcon" , true);

            SPEC = builder.build();
        }
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Server.SPEC);
    }
}
