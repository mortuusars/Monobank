package io.github.mortuusars.monobank;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.monobank.config.Configuration;
import io.github.mortuusars.monobank.event.ClientSetup;
import io.github.mortuusars.monobank.event.CommonEvents;
import io.github.mortuusars.monobank.world.VillageStructures;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Monobank.ID)
public class Monobank
{
    public static final String ID = "monobank";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final boolean IN_DEBUG = false;

    public Monobank()
    {
        Configuration.register();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modEventBus.addListener(ClientSetup::init);
            modEventBus.addListener(ClientSetup::registerRenderers);
            modEventBus.addListener(ClientSetup::registerModels);
        });

        modEventBus.addListener(CommonEvents::onCommonSetup);

        Registry.register(modEventBus);

        MinecraftForge.EVENT_BUS.addListener(CommonEvents::onEntityInteractEvent);
        MinecraftForge.EVENT_BUS.addListener(CommonEvents::onBlockRightClick);
        MinecraftForge.EVENT_BUS.addListener(CommonEvents::onBlockBroken);

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
