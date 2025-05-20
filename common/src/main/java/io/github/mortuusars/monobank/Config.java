package io.github.mortuusars.monobank;

import io.github.mortuusars.monobank.integration.thief.ThiefCrime;
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
        public static final ModConfigSpec.BooleanValue CAN_RELOCATE_OTHER_PLAYERS_MONOBANK;
        public static final ModConfigSpec.BooleanValue CAN_REPLACE_OTHER_PLAYERS_LOCKS;

        // Combination
        public static final ModConfigSpec.BooleanValue COMBINATION_ENABLED;
        public static final ModConfigSpec.BooleanValue SKIP_COMBINATION_IF_OWNER;
        public static final ModConfigSpec.BooleanValue SKIP_COMBINATION_IF_NOT_OWNER;
        public static final ModConfigSpec.BooleanValue SKIP_COMBINATION_FOR_NPC_OWNED;
        public static final ModConfigSpec.BooleanValue PLAYER_UNLOCKING;
        public static final ModConfigSpec.BooleanValue COMBINATION_HINT_ICON;
        public static final ModConfigSpec.DoubleValue COMBINATION_HINT_ICON_OPACITY;
        public static final ModConfigSpec.DoubleValue COMBINATION_HINT_TOOLTIP_OBFUSCATION;

        // Thief
        public static final ModConfigSpec.EnumValue<ThiefCrime> THIEF_CRIME_FOR_UNLOCKING_ATTEMPT;
        public static final ModConfigSpec.EnumValue<ThiefCrime> THIEF_CRIME_FOR_UNLOCKING;
        public static final ModConfigSpec.EnumValue<ThiefCrime> THIEF_CRIME_FOR_OPENING;

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
                CAN_RELOCATE_OTHER_PLAYERS_MONOBANK = builder
                        .comment(" Players can break and pickup a Monobank that is owned by another player.",
                                " Default: false")
                        .define("can_break_other_players_monobanks", false);
                CAN_REPLACE_OTHER_PLAYERS_LOCKS = builder
                        .comment(" Players can change lock in a Monobank that is owned by another player.",
                                " Default: false")
                        .define("can_replace_other_players_locks", false);

                {
                    builder.push("combination");

                    COMBINATION_ENABLED = builder
                            .comment(" Combination must be entered to unlock a Monobank.",
                                    " Default: true")
                            .define("enabled", true);

                    PLAYER_UNLOCKING = builder
                            .comment(" Players can enter a combination to unlock a Monobank that is owned by someone else.",
                                    " If disabled, only owner can open their Monobank.",
                                    " NPC owned Monobanks are not affected by this. They can still be unlocked by everyone.",
                                    " Default: true")
                            .define("player_unlocking", true);

                    SKIP_COMBINATION_IF_OWNER = builder
                            .comment(" Owner can unlock their Monobank without entering a combination.",
                                    " Default: true")
                            .define("skip_combination_if_owner", true);

                    SKIP_COMBINATION_IF_NOT_OWNER = builder
                            .comment(" Any player can unlock a Monobank (player owned) without entering a combination.",
                                    " Default: false")
                            .define("skip_combination_if_not_owner", false);

                    SKIP_COMBINATION_FOR_NPC_OWNED = builder
                            .comment(" NPC-owned Monobanks (villages, etc) can be unlocked without entering a combination.",
                                    " Default: false")
                            .define("skip_combination_for_npc_owned", false);

                    builder.pop();
                }

                {
                    builder.push("combination_ui");

                    COMBINATION_HINT_ICON = builder
                            .comment(" Show partially visible hint icon in combination slot.",
                                    " Default: true")
                            .define("hint_icon", true);

                    COMBINATION_HINT_ICON_OPACITY = builder
                            .comment(" Opacity percentage of the hint icon.")
                            .defineInRange("hint_icon_opacity", 0.3, 0.0, 1.0);

                    COMBINATION_HINT_TOOLTIP_OBFUSCATION = builder
                            .comment(" Chance of a letter being obfuscated in slot tooltip.")
                            .defineInRange("tooltip_obfuscation_chance", 0.5, 0.0, 1.0);

                    builder.pop();
                }

                builder.pop();
            }

            {
                builder.push("thief");

                THIEF_CRIME_FOR_UNLOCKING_ATTEMPT = builder
                        .comment(" Crime severity for attempting to unlock a Monobank (opening combination UI).",
                                 " Default: LIGHT")
                        .defineEnum("crime_severity_for_unlocking_attempt", ThiefCrime.LIGHT);

                THIEF_CRIME_FOR_UNLOCKING = builder
                        .comment(" Crime severity for unlocking a Monobank.",
                                 " Default: MEDIUM")
                        .defineEnum("crime_severity_for_unlocking", ThiefCrime.MEDIUM);

                THIEF_CRIME_FOR_OPENING = builder
                        .comment(" Crime severity for opening a Monobank.",
                                 " Default: HEAVY")
                        .defineEnum("crime_severity_for_opening", ThiefCrime.HEAVY);

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