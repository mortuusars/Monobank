package io.github.mortuusars.monobank.network.fabric;

import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.fabric.MonobankFabric;
import io.github.mortuusars.monobank.network.packet.Packet;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class PacketsImpl {
    public static void sendToServer(Packet packet) {
        FabricC2SPackets.sendToServer(packet);
    }

    public static void sendToClient(Packet packet, ServerPlayer player) {
        ServerPlayNetworking.send(player, packet);
    }

    public static void sendToClients(Packet packet, Predicate<ServerPlayer> filter) {
        if (MonobankFabric.server == null) {
            Monobank.LOGGER.error("Cannot send a packet to players. Server is not available.");
            return;
        }

        for (ServerPlayer player : MonobankFabric.server.getPlayerList().getPlayers()) {
            if (filter.test(player)) {
                sendToClient(packet, player);
            }
        }
    }

    public static void sendToAllClients(Packet packet) {
        if (MonobankFabric.server == null) {
            Monobank.LOGGER.error("Cannot send a packet to all players. Server is not available.");
            return;
        }

        for (ServerPlayer player : MonobankFabric.server.getPlayerList().getPlayers()) {
            sendToClient(packet, player);
        }
    }

    public static void sendToPlayersNear(Packet packet, @NotNull ServerLevel level, @Nullable ServerPlayer excludedPlayer,
                                         double x, double y, double z, double radius) {
        sendToClients(packet, player -> {
            if (player != excludedPlayer && player.level().dimension() == level.dimension()) {
                double d0 = x - player.getX();
                double d1 = y - player.getY();
                double d2 = z - player.getZ();
                return d0 * d0 + d1 * d1 + d2 * d2 < radius * radius;
            }

            return false;
        });
    }
}
