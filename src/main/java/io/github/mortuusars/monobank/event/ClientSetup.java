package io.github.mortuusars.monobank.event;

import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.Registry;
import io.github.mortuusars.monobank.client.gui.component.CombinationTooltip;
import io.github.mortuusars.monobank.content.monobank.MonobankBlockEntity;
import io.github.mortuusars.monobank.content.monobank.component.Lock;
import io.github.mortuusars.monobank.content.monobank.lock_replacement.LockReplacementScreen;
import io.github.mortuusars.monobank.content.monobank.renderer.MonobankRenderer;
import io.github.mortuusars.monobank.content.monobank.MonobankScreen;
import io.github.mortuusars.monobank.content.monobank.unlocking.UnlockingScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {
    public static void init(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(Registry.MenuTypes.MONOBANK.get(), MonobankScreen::new);
            MenuScreens.register(Registry.MenuTypes.MONOBANK_UNLOCKING.get(), UnlockingScreen::new);
            MenuScreens.register(Registry.MenuTypes.MONOBANK_LOCK_REPLACEMENT.get(), LockReplacementScreen::new);

            ItemProperties.register(Registry.Items.MONOBANK.get(), new ResourceLocation("locked"), (stack, level, entity, seed) -> {
                if (stack.hasTag() && stack.getTag().contains("BlockEntityTag", CompoundTag.TAG_COMPOUND)) {
                    CompoundTag blockEntityTag = stack.getTag().getCompound("BlockEntityTag");
                    if (blockEntityTag.contains(MonobankBlockEntity.LOCK_TAG, CompoundTag.TAG_COMPOUND)) {
                        CompoundTag lock = blockEntityTag.getCompound(MonobankBlockEntity.LOCK_TAG);
                        return lock.getBoolean(Lock.LOCKED_TAG) ? 1f : 0f;
                    }
                }
                return 0f;
            });

            MinecraftForgeClient.registerTooltipComponentFactory(CombinationTooltip.class, combinationTooltip -> combinationTooltip);
        });
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registry.BlockEntityTypes.MONOBANK.get(), MonobankRenderer::new);
    }

    public static void registerModels(ModelRegistryEvent ignoredEvent) {
        ForgeModelBakery.addSpecialModel(Monobank.resource("block/monobank_door"));
    }
}
