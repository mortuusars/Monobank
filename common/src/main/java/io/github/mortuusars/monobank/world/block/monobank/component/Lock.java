package io.github.mortuusars.monobank.world.block.monobank.component;

import com.mojang.datafixers.util.Either;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.Supplier;

/**
 * Manages Monobank locking and unlocking.
 */
public class Lock {
    public static final String LOCKED_TAG = "Locked";
    public static final String COMBINATION_TABLE_TAG = "CombinationTable";
    public static final String COMBINATION_TAG = "Combination";

    // Serialized fields:
    private Either<ResourceKey<LootTable>, Combination> combination;
    private boolean locked = false;

    private final BlockPos pos;
    private final Runnable onLockedChanged;
    private final Supplier<Level> levelSupplier;

    /**
     * @param pos Position of a block entity.
     * @param onLockChanged Code to run when lock is locked or unlocked.
     */
    public Lock(BlockPos pos, Runnable onLockChanged, Supplier<Level> levelSupplier) {
        this.pos = pos;
        this.onLockedChanged = onLockChanged;
        this.levelSupplier = levelSupplier;
        this.combination = Either.right(Combination.empty());
    }

    public void setCombination(Item first, Item second, Item third) {
        this.combination = Either.right(new Combination(List.of(first, second, third)));
    }

    public void setCombination(Combination combination) {
        this.combination = Either.right(combination);
    }

    public void setCombinationTable(ResourceKey<LootTable> combinationLootTable) {
        combination = Either.left(combinationLootTable);
    }

    public boolean hasCombinationOrCombinationTable() {
        boolean b = !combination.right().orElse(Combination.empty()).isEmpty();
        return combination.left().isPresent() || b;
    }

    public Combination getCombination() {
        if (combination.left().isPresent() && tryUnpackCombinationTable())
            Objects.requireNonNull(levelSupplier.get().getBlockEntity(pos)).setChanged(); // Save block entity
        return combination.right().orElse(Combination.empty());
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        onLockedChanged.run();
    }

    // -- Save

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(LOCKED_TAG, locked);
        combination
                .ifLeft(lootTable -> tag.putString(COMBINATION_TABLE_TAG, lootTable.location().toString()))
                .ifRight(combination -> tag.put(COMBINATION_TAG, combination.serializeNBT()));
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag.isEmpty())
            return;

        this.locked = tag.getBoolean(LOCKED_TAG);

        if (tag.contains(COMBINATION_TABLE_TAG, CompoundTag.TAG_STRING)) {
            ResourceLocation location = ResourceLocation.parse(tag.getString(COMBINATION_TABLE_TAG));
            ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, location);
            this.combination = Either.left(key);
        } else if (tag.contains(COMBINATION_TAG, CompoundTag.TAG_LIST)) {
            Combination combination = this.combination.right().orElse(Combination.empty());
            combination.deserializeNBT(tag.getList(COMBINATION_TAG, CompoundTag.TAG_STRING));
            this.combination = Either.right(combination);
        }
    }

    public boolean tryUnpackCombinationTable() {
        Level level = levelSupplier.get();
        if (level.getServer() == null || level.isClientSide)
            return false;

        Optional<ResourceKey<LootTable>> combinationTable = combination.left();
        if (combinationTable.isEmpty()) {
            return false;
        }

        LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(combinationTable.get());
        LootParams lootParams = new LootParams.Builder((ServerLevel) level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.pos))
                .create(LootContextParamSets.CHEST);
        List<ItemStack> randomItems = lootTable.getRandomItems(lootParams);

        ArrayList<Item> newCombination = new ArrayList<>();
        for (int i = 0; i < Combination.SIZE; i++) {
            if (randomItems.size() <= i)
                newCombination.add(Items.AIR);
            else
                newCombination.add(randomItems.get(i).getItem());
        }
        Collections.shuffle(newCombination);
        this.combination = Either.right(new Combination(newCombination));

        if (level.getBlockEntity(pos) instanceof MonobankBlockEntity monobankBlockEntity)
            monobankBlockEntity.setChanged();

        return true;
    }
}
