package io.github.mortuusars.monobank.fabric;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class MonobankMixinPlugin implements IMixinConfigPlugin {

    private static final Map<String, Supplier<Boolean>> CONDITIONS = ImmutableMap.of(
            "io.github.mortuusars.exposure.fabric.mixin.create.RecipeTypesMixin", MonobankMixinPlugin::isCorrectCreateVersion,
            "io.github.mortuusars.exposure.fabric.mixin.create.SpoutDevelopingMixin", MonobankMixinPlugin::isCorrectCreateVersion,
            "io.github.mortuusars.exposure.fabric.mixin.create.CreateEmiPluginMixin", () -> FabricLoader.getInstance().isModLoaded("emi") && isCorrectCreateVersion()
    );

    private static boolean isCorrectCreateVersion() {
        return FabricLoader.getInstance().getModContainer("create")
                        .map(c -> c.getMetadata().getVersion().getFriendlyString().startsWith("0.5.1-f"))
                        .orElse(false);
    }

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return CONDITIONS.getOrDefault(mixinClassName, () -> true).get();
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
