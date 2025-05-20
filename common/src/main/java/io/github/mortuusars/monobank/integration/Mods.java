package io.github.mortuusars.monobank.integration;

import io.github.mortuusars.monobank.PlatformHelper;

public class Mods {
    public static final Mod THIEF = new Mod("thief");

    public record Mod(String id) {
        public boolean isLoaded() {
            return PlatformHelper.isModLoaded(id);
        }

        public boolean isLoading() {
            return PlatformHelper.isModLoading(id);
        }
    }
}
