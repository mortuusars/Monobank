package io.github.mortuusars.monobank.neoforge;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.neoforge.common.ItemAbilities;

import java.util.function.Consumer;

public class PlatformHelperImpl {
    public static boolean isInDevEnv() {
        return !FMLEnvironment.production;
    }

    public static boolean canShear(ItemStack stack) {
        return stack.canPerformAction(ItemAbilities.SHEARS_REMOVE_ARMOR);
    }

    public static void openMenu(ServerPlayer serverPlayer, MenuProvider menuProvider, Consumer<RegistryFriendlyByteBuf> extraDataWriter) {
        serverPlayer.openMenu(menuProvider, extraDataWriter);
    }

    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public static boolean isModLoading(String modId) {
        return LoadingModList.get().getModFileById(modId) != null;
    }
}
