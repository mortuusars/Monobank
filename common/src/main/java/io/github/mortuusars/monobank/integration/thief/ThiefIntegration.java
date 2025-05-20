package io.github.mortuusars.monobank.integration.thief;

import io.github.mortuusars.monobank.Config;
import io.github.mortuusars.monobank.world.block.monobank.MonobankBlockEntity;
import io.github.mortuusars.thief.world.Crime;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class ThiefIntegration {
    public static void unlockingGuiOpened(ServerPlayer player, MonobankBlockEntity blockEntity) {
        convertCrime(Config.Server.THIEF_CRIME_FOR_UNLOCKING_GUI.get()).ifPresent(crime ->
                crime.commit(player.serverLevel(), player, blockEntity.getBlockPos()));
    }

    public static void unlocked(ServerPlayer player, MonobankBlockEntity blockEntity) {
        convertCrime(Config.Server.THIEF_CRIME_FOR_UNLOCKING.get()).ifPresent(crime ->
                crime.commit(player.serverLevel(), player, blockEntity.getBlockPos()));
    }

    public static void opened(ServerPlayer player, MonobankBlockEntity blockEntity) {
        convertCrime(Config.Server.THIEF_CRIME_FOR_OPENING.get()).ifPresent(crime ->
                crime.commit(player.serverLevel(), player, blockEntity.getBlockPos()));
    }

    public static Optional<Crime> convertCrime(ThiefCrime crime) {
        return switch (crime) {
            case NONE -> Optional.empty();
            case LIGHT -> Optional.of(Crime.LIGHT);
            case MEDIUM -> Optional.of(Crime.MEDIUM);
            case HEAVY -> Optional.of(Crime.HEAVY);
        };
    }
}
