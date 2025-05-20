package io.github.mortuusars.monobank.integration;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.LoadingModList;

public class Mods {
    public static final Mod THIEF = new Mod("thief");

    public record Mod(String id) {
        public boolean isLoaded() {
            return ModList.get().isLoaded(id);
        }

        public boolean isLoading() {
            return LoadingModList.get().getModFileById(id) != null;
        }
    }
}