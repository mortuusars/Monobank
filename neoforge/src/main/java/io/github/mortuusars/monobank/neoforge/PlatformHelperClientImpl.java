package io.github.mortuusars.monobank.neoforge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;

public class PlatformHelperClientImpl {
    public static BakedModel getModel(ModelResourceLocation model) {
        return Minecraft.getInstance().getModelManager().getModel(model);
    }
}
