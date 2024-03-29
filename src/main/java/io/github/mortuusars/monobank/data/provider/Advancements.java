package io.github.mortuusars.monobank.data.provider;

import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.Registry;
import io.github.mortuusars.monobank.content.advancement.trigger.MonobankInventoryChangedTrigger;
import io.github.mortuusars.monobank.content.advancement.trigger.MonobankLockReplacedTrigger;
import io.github.mortuusars.monobank.content.advancement.trigger.MonobankUnlockedTrigger;
import io.github.mortuusars.monobank.content.monobank.MonobankBlockEntity;
import io.github.mortuusars.monobank.content.monobank.component.Lock;
import io.github.mortuusars.monobank.util.TextUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Advancements extends AdvancementProvider
{
    public Advancements(DataGenerator dataGenerator, CompletableFuture<HolderLookup.Provider> provider, ExistingFileHelper existingFileHelper) {
        super(dataGenerator.getPackOutput(), provider, List.of(new MonobankAdvancements(existingFileHelper)));
    }

    public static class MonobankAdvancements implements AdvancementSubProvider
    {
        private ExistingFileHelper existingFileHelper;

        public MonobankAdvancements(ExistingFileHelper existingFileHelper) {
            this.existingFileHelper = existingFileHelper;
        }

        @Override
        public void generate(HolderLookup.Provider pRegistries, Consumer<Advancement> advancementConsumer) {
            ItemStack lockedMonobankItemStack = new ItemStack(Registry.Items.MONOBANK.get());
            CompoundTag blockEntityTag = new CompoundTag();
            Lock lock = new Lock(BlockPos.ZERO, () -> {}, integer -> {}, () -> null);
            lock.setLocked(true);
            blockEntityTag.put(MonobankBlockEntity.LOCK_TAG, lock.serializeNBT());
            CompoundTag stackTag = new CompoundTag();
            stackTag.put("BlockEntityTag", blockEntityTag);
            lockedMonobankItemStack.setTag(stackTag);

            Advancement monobank = Advancement.Builder.advancement()
                    .parent(new ResourceLocation("minecraft:adventure/root"))
                    .display(lockedMonobankItemStack,
                            TextUtil.translate("advancement.monobank.title"),
                            TextUtil.translate("advancement.monobank.description"),
                            null,
                            FrameType.TASK, true, false, false)
                    .addCriterion("monobank_get", InventoryChangeTrigger.TriggerInstance.hasItems(Registry.Items.MONOBANK.get()))
                    .save(advancementConsumer, Monobank.resource("adventure/monobank"), existingFileHelper);

            Advancement unlock = Advancement.Builder.advancement()
                    .parent(monobank)
                    .display(Registry.Items.MONOBANK.get(),
                            TextUtil.translate("advancement.unlock.title"),
                            TextUtil.translate("advancement.unlock.description"),
                            null,
                            FrameType.TASK, true, false, false)
                    .addCriterion("monobank_unlock", new MonobankUnlockedTrigger.TriggerInstance(ContextAwarePredicate.ANY))
                    .save(advancementConsumer, Monobank.resource("adventure/monobank_unlock"), existingFileHelper);

            Advancement replace_lock = Advancement.Builder.advancement()
                    .parent(unlock)
                    .display(Registry.Items.REPLACEMENT_LOCK.get(),
                            TextUtil.translate("advancement.replace_lock.title"),
                            TextUtil.translate("advancement.replace_lock.description"),
                            null,
                            FrameType.TASK, true, false, false)
                    .addCriterion("monobank_replace_lock", new MonobankLockReplacedTrigger.TriggerInstance(ContextAwarePredicate.ANY))
                    .save(advancementConsumer, Monobank.resource("adventure/monobank_replace_lock"), existingFileHelper);

            Advancement full = Advancement.Builder.advancement()
                    .parent(replace_lock)
                    .display(lockedMonobankItemStack,
                            TextUtil.translate("advancement.monobank_full.title"),
                            TextUtil.translate("advancement.monobank_full.description"),
                            null,
                            FrameType.CHALLENGE, true, true, false)
                    .addCriterion("monobank_full", MonobankInventoryChangedTrigger.TriggerInstance.full())
                    .save(advancementConsumer, Monobank.resource("adventure/monobank_full").toString());
        }
    }
}
