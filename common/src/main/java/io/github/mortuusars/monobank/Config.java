package io.github.mortuusars.monobank;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Using ForgeConfigApiPort on fabric allows using forge config in both environments and without extra dependencies on forge.
 */
public class Config {
    public static class Server {
        public static final ModConfigSpec SPEC;

        // Monobank
        public static final ModConfigSpec.IntValue MONOBANK_CAPACITY;

        // Lock
        public static final ModConfigSpec.BooleanValue LOCK_PREVENTS_ITEM_INSERTION;
        public static final ModConfigSpec.BooleanValue LOCK_PREVENTS_ITEM_EXTRACTION;
        public static final ModConfigSpec.BooleanValue ANYONE_CAN_UNLOCK_WITHOUT_COMBINATION;
        public static final ModConfigSpec.BooleanValue OWNER_CAN_UNLOCK_WITHOUT_COMBINATION;
        public static final ModConfigSpec.BooleanValue CAN_RELOCATE_OTHER_PLAYERS_BANK;
        public static final ModConfigSpec.BooleanValue CAN_REPLACE_OTHER_PLAYERS_LOCKS;

        // Combination
        public static final ModConfigSpec.BooleanValue COMBINATION_SLOT_ICONS;
        public static final ModConfigSpec.IntValue COMBINATION_SLOT_ICONS_OPACITY;
        public static final ModConfigSpec.DoubleValue COMBINATION_SLOT_TOOLTIP_OBFUSCATION;

        // Structures
        public static final ModConfigSpec.BooleanValue GENERATE_VILLAGE_STRUCTURES;
        public static final ModConfigSpec.IntValue VAULT_WEIGHT;

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            MONOBANK_CAPACITY = builder
                    .comment(" Maximum amount of items that can be stored in a Monobank.")
                    .defineInRange("capacity", 8192, 1, Integer.MAX_VALUE);

            {
                builder.push("lock");

                LOCK_PREVENTS_ITEM_INSERTION = builder
                        .comment(" Locking a Monobank prevents item insertion (with Hoppers, etc)",
                                " Default: true")
                        .define("prevent_insertion", true);
                LOCK_PREVENTS_ITEM_EXTRACTION = builder
                        .comment(" Locking a Monobank prevents item extraction (with Hoppers, etc).",
                                " WARNING: when disabled, other players would be able to steal items from a Monobank without entering a combination and opening it.",
                                " Default: true")
                        .define("prevent_extraction", true);
                ANYONE_CAN_UNLOCK_WITHOUT_COMBINATION = builder
                        .comment(" Any player can unlock a Monobank without entering a combination.",
                                " Default: false")
                        .define("anyone_can_unlock_without_combination", false);
                OWNER_CAN_UNLOCK_WITHOUT_COMBINATION = builder
                        .comment(" Owner can unlock their Monobank without entering a combination.",
                                " Default: true")
                        .define("owner_can_unlock_without_combination", true);
                CAN_RELOCATE_OTHER_PLAYERS_BANK = builder
                        .comment(" Players can break and pickup a Monobank that is owned by another player.",
                                " Default: false")
                        .define("can_break_other_players_banks", false);
                CAN_REPLACE_OTHER_PLAYERS_LOCKS = builder
                        .comment(" Players can change lock in a Monobank that is owned by another player.",
                                " Default: false")
                        .define("can_replace_other_players_locks", false);

                builder.pop();
            }

            {
                builder.push("combination");

                COMBINATION_SLOT_ICONS = builder
                        .comment(" Show partially visible item icon in combination slot.",
                                " Default: true")
                        .define("slot_icon", true);

                COMBINATION_SLOT_ICONS_OPACITY = builder
                        .comment(" Opacity percentage of the slot icon.")
                        .defineInRange("slot_icon_opacity", 30, 0, 100);

                COMBINATION_SLOT_TOOLTIP_OBFUSCATION = builder
                        .comment(" Chance of a letter being obfuscated in slot tooltip.")
                        .defineInRange("tooltip_obfuscation_chance", 0.5, 0.0, 1.0);

                builder.pop();
            }

            {
                builder.push("structures");

                GENERATE_VILLAGE_STRUCTURES = builder
                        .comment(" Vault buildings will generate in villages.",
                                " Default: true")
                        .define("generate_vaults", true);

                VAULT_WEIGHT = builder
                        .comment(" Vault building generation weight. Larger number = more chances to generate.")
                        .defineInRange("vault_weight", 15, 1, Integer.MAX_VALUE);

                builder.pop();
            }

            SPEC = builder.build();
        }
    }

    public static class Client {
        public static final ModConfigSpec SPEC;

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            SPEC = builder.build();
        }
    }
}

/*THIEF_ENABLED = builder
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
                .define("VillagersCheckRangeBeforeHatingThieves", false);*/
