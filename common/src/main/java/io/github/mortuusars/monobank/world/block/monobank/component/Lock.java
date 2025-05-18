package io.github.mortuusars.monobank.world.block.monobank.component;

import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.world.block.monobank.MonobankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Manages Monobank locking and unlocking.
 */
public class Lock {
    public static final String COMBINATION_TABLE_TAG = "CombinationTable";
    public static final String COMBINATION_TAG = "Combination";
    public static final String HAS_COMBINATION_TAG = "HasCombination";
    public static final String LOCKED_TAG = "Locked";
    public static final String UNLOCKING_TIME_TAG = "UnlockingTime";
    public static final String UNLOCKING_COUNTDOWN_TAG = "UnlockingCountdown";

    protected final Runnable onLockedChanged;

    protected @Nullable ResourceKey<LootTable> combinationTable = null; // set to 'null' when unpacked
    protected Combination combination = Combination.EMPTY;
    protected boolean hasCombination = false;
    protected boolean locked = false;
    protected int unlockingTime = 0;
    protected int unlockingCountdown = 0;

    public Lock(Runnable onLockChanged) {
        this.onLockedChanged = onLockChanged;
    }

    public @Nullable ResourceKey<LootTable> getCombinationTable() {
        return combinationTable;
    }

    public void setCombinationTable(@NotNull ResourceKey<LootTable> combinationTable) {
        this.combinationTable = combinationTable;
    }

    public Combination getCombination() {
        return combination;
    }

    public void setCombination(Combination combination) {
        this.combination = combination;
        this.combinationTable = null;
        this.hasCombination = true;
    }

    public boolean hasCombination() {
        return hasCombination;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        onLockedChanged.run();
    }

    public boolean isUnlocking() {
        return unlockingCountdown > 0;
    }

    public int getUnlockingTime() {
        return unlockingTime;
    }

    public int getUnlockingCountdown() {
        return unlockingCountdown;
    }

    public void startUnlocking(int ticks) {
        unlockingTime = ticks;
        unlockingCountdown = ticks;
    }

    // -- Tick

    public void tick(ServerLevel level, MonobankBlockEntity blockEntity) {
        if (unlockingCountdown > 0) {
            // Calculating frequency of clicks (closer to unlocking -> more time between clicks):
            int max = (int) Math.ceil(Math.log(unlockingTime)) + 1;
            int current = (int) Math.ceil(Math.log(unlockingCountdown));
            int freq = max - current;
            if (unlockingCountdown % freq == 0) {
                blockEntity.playSoundAtDoor(Monobank.SoundEvents.MONOBANK_CLICK.get(),
                        0.5f, level.random.nextFloat() * 0.1f + 0.95f);
            }

            unlockingCountdown--;

            if (unlockingCountdown <= 0) {
                setLocked(false);
            }
        }
    }

    // -- Save

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        if (combinationTable != null) {
            tag.putString(COMBINATION_TABLE_TAG, combinationTable.location().toString());
        }
        if (!combination.isEmpty()) {
            tag.put(COMBINATION_TAG, combination.save());
        }
        if (hasCombination) {
            tag.putBoolean(HAS_COMBINATION_TAG, true);
        }
        if (locked) {
            tag.putBoolean(LOCKED_TAG, true);
        }
        if (unlockingTime > 0) {
            tag.putInt(UNLOCKING_TIME_TAG, unlockingTime);
        }
        if (unlockingCountdown > 0) {
            tag.putInt(UNLOCKING_COUNTDOWN_TAG, unlockingCountdown);
        }
        return tag;
    }

    public void load(CompoundTag tag) {
        if (tag.isEmpty()) return;

        if (tag.contains(COMBINATION_TABLE_TAG, CompoundTag.TAG_STRING)) {
            ResourceLocation location = ResourceLocation.parse(tag.getString(COMBINATION_TABLE_TAG));
            combinationTable = ResourceKey.create(Registries.LOOT_TABLE, location);
        }

        if (tag.contains(COMBINATION_TAG, CompoundTag.TAG_LIST)) {
            combination = Combination.load(tag.getList(COMBINATION_TAG, CompoundTag.TAG_STRING));
        }
        hasCombination = tag.getBoolean(HAS_COMBINATION_TAG);
        locked = tag.getBoolean(LOCKED_TAG);
        unlockingTime = tag.getInt(UNLOCKING_TIME_TAG);
        unlockingCountdown = tag.getInt(UNLOCKING_COUNTDOWN_TAG);
    }

    // -- Loot Table

    @SuppressWarnings("UnusedReturnValue")
    public boolean unpackCombinationTableIfNeeded(ServerLevel level, BlockPos pos) {
        if (!hasCombination && combinationTable != null) {
            return tryUnpackCombinationTable(level, pos);
        }
        return false;
    }

    public boolean tryUnpackCombinationTable(ServerLevel level, BlockPos pos) {
        List<ItemStack> items = getItemsFromLootTable(level, pos, combinationTable);

        ArrayList<Item> newCombination = new ArrayList<>();
        for (int i = 0; i < Combination.SIZE; i++) {
            Item item = items.size() <= i ? Items.AIR : items.get(i).getItem();
            newCombination.add(item);
        }
        Collections.shuffle(newCombination);

        Item first = !items.isEmpty() ? items.get(0).getItem() : Items.AIR;
        Item second = items.size() >= 2 ? items.get(1).getItem() : Items.AIR;
        Item third = items.size() >= 3 ? items.get(2).getItem() : Items.AIR;

        this.combination = new Combination(first, second, third);

        hasCombination = true;

        if (level.getBlockEntity(pos) instanceof MonobankBlockEntity monobankBlockEntity) {
            monobankBlockEntity.setChanged();
        }

        return true;
    }

    protected @NotNull List<ItemStack> getItemsFromLootTable(ServerLevel level, BlockPos pos, ResourceKey<LootTable> lootTable) {
        LootParams lootParams = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .create(LootContextParamSets.CHEST);
        LootTable table = level.getServer().reloadableRegistries().getLootTable(lootTable);
        return table.getRandomItems(lootParams);
    }
}
