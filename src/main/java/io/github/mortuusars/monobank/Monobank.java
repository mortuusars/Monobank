package io.github.mortuusars.monobank;

import com.mojang.brigadier.StringReader;
import io.github.mortuusars.monobank.config.Configuration;
import io.github.mortuusars.monobank.content.effect.Thief;
import io.github.mortuusars.monobank.event.ClientSetup;
import io.github.mortuusars.monobank.event.CommonSetup;
import io.github.mortuusars.monobank.world.VillageStructures;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Monobank.ID)
public class Monobank
{
    public static final String ID = "monobank";
    public static final boolean IN_DEBUG = true;

    public Monobank()
    {
        Configuration.register();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modEventBus.addListener(ClientSetup::init);
            modEventBus.addListener(ClientSetup::registerRenderers);
            modEventBus.addListener(ClientSetup::registerModels);
        });

        modEventBus.addListener(CommonSetup::onCommonSetup);

        Registry.register(modEventBus);

        MinecraftForge.EVENT_BUS.addListener(Thief::onEntityInteractEvent);
        MinecraftForge.EVENT_BUS.addListener(Thief::onBlockRightClick);
        MinecraftForge.EVENT_BUS.addListener(Thief::onBlockBroken);

        MinecraftForge.EVENT_BUS.addListener(VillageStructures::addVillageStructures);

        MinecraftForge.EVENT_BUS.register(this);

        // Plains Village locked monobank with tables:
        // /give @s monobank:monobank{BlockEntityTag:{Owner:{Type:"npc"},Lock:{Locked:1b,CombinationTable:"monobank:combination/village/plains"},LootTable:"monobank:monobank/village/plains"}}

        // Plains Village UNLOCKED monobank with tables:
        // /give @s monobank:monobank{BlockEntityTag:{Owner:{Type:"npc"},Lock:{Locked:0b,CombinationTable:"monobank:combination/village/plains"},LootTable:"monobank:monobank/village/plains"}}

        // Plains Village UNLOCKED EMPTY monobank with tables:
        // /give @s monobank:monobank{BlockEntityTag:{Owner:{Type:"npc"},Lock:{Locked:0b,CombinationTable:"monobank:combination/village/plains"}}}
    }

    /**
     * Creates resource location in the mod namespace with the given path.
     */
    public static ResourceLocation resource(String path) {
        return new ResourceLocation(ID, path);
    }

    public static int getSlotCapacity() {
        return Configuration.MONOBANK_CAPACITY.get();
    }
}
