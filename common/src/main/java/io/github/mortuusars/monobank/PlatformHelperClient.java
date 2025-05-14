package io.github.mortuusars.monobank;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;

public class PlatformHelperClient {
    @ExpectPlatform
    public static BakedModel getModel(ModelResourceLocation model) {
        throw new AssertionError();
    }
}
