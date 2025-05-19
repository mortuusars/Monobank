package io.github.mortuusars.monobank.event;

import io.github.mortuusars.monobank.world.VillageStructures;
import net.minecraft.server.MinecraftServer;

public class ServerEvents {
    public static void serverStart(MinecraftServer server) {
        VillageStructures.addVillageStructures(server);
    }
}
