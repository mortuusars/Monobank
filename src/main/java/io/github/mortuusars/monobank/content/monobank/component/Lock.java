package io.github.mortuusars.monobank.content.monobank.component;

import com.mojang.datafixers.util.Either;
import io.github.mortuusars.monobank.content.monobank.unlocking.Combination;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Manages Monobank locking and unlocking.
 */
public class Lock {
    public static final String LOCKED_TAG = "Locked";
    public static final String COMBINATION_TABLE_TAG = "CombinationTable";
    public static final String COMBINATION_TAG = "Combination";
    public static final String INVENTORY_TAG = "Inventory";

    // Serialized fields:
    private Either<ResourceLocation, Combination> combination;
    private final ItemStackHandler inventory;
    private boolean locked = false;

    private BlockPos pos;
    private Runnable onLockedChanged;
    private Consumer<Integer> onLockInventoryChanged;
    private Supplier<Level> levelSupplier;

    /**
     * @param pos Position of a block entity.
     * @param onLockChanged Code to run when lock is locked or unlocked.
     * @param onLockInventoryChanged Code to run when lock inventory changes. Changed slot will be provided.
     */
    public Lock(BlockPos pos, Runnable onLockChanged, Consumer<Integer> onLockInventoryChanged, Supplier<Level> levelSupplier) {
        this.pos = pos;
        this.onLockedChanged = onLockChanged;
        this.onLockInventoryChanged = onLockInventoryChanged;
        this.levelSupplier = levelSupplier;
        this.combination = Either.right(Combination.empty());
        this.inventory = createLockItemHandler();
    }

    public IItemHandler getInventory() {
        return inventory;
    }

    public void setCombination(Item first, Item second, Item third) {
        this.combination = Either.right(new Combination(List.of(first, second, third)));
    }

    public void setCombinationTable(ResourceLocation combinationLootTable) {
        combination = Either.left(combinationLootTable);
    }

    public boolean hasCombinationOrCombinationTable() {
        boolean b = !combination.right().orElse(Combination.empty()).isEmpty();
        return combination.left().isPresent() || b;
    }

    public Combination getCombination() {
        if (combination.left().isPresent() && tryUnpackCombinationTable())
            levelSupplier.get().getBlockEntity(pos).setChanged(); // Save block entity
        return combination.right().orElse(Combination.empty());
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        onLockedChanged.run();
    }


    // Save

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(LOCKED_TAG, locked);
        combination
                .ifLeft(lootTable -> tag.putString(COMBINATION_TABLE_TAG, lootTable.toString()))
                .ifRight(combination -> tag.put(COMBINATION_TAG, combination.serializeNBT()));
        if (hasItemsInInventory())
            tag.put(INVENTORY_TAG, inventory.serializeNBT());
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag.isEmpty())
            return;

        this.locked = tag.getBoolean(LOCKED_TAG);

        if (tag.contains(COMBINATION_TABLE_TAG, CompoundTag.TAG_STRING))
            this.combination = Either.left(new ResourceLocation(tag.getString(COMBINATION_TABLE_TAG)));
        else if (tag.contains(COMBINATION_TAG, CompoundTag.TAG_LIST)) {
            Combination combination = this.combination.right().orElse(Combination.empty());
            combination.deserializeNBT(tag.getList(COMBINATION_TAG, CompoundTag.TAG_STRING));
            this.combination = Either.right(combination);
        }

        if (tag.contains(INVENTORY_TAG, CompoundTag.TAG_COMPOUND))
            this.inventory.deserializeNBT(tag.getCompound(INVENTORY_TAG));
    }

    public boolean tryUnpackCombinationTable() {
        Level level = levelSupplier.get();

        Optional<ResourceLocation> combinationTable = combination.left();
        if (!combinationTable.isPresent())
            return false;

        ResourceLocation lootTable = combinationTable.get();

        if (level.getServer() == null)
            return false;

        LootTable loottable = level.getServer().getLootTables().get(lootTable);
        LootContext lootContext = new LootContext.Builder((ServerLevel)level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.pos))
                .create(LootContextParamSets.CHEST);

        List<ItemStack> randomItems = loottable.getRandomItems(lootContext);
        ArrayList<Item> newCombination = new ArrayList<>();
        for (int i = 0; i < Combination.SIZE; i++) {
            if (randomItems.size() <= i)
                newCombination.add(Items.AIR);
            else
                newCombination.add(randomItems.get(i).getItem());
        }
        Collections.shuffle(newCombination);
        this.combination = Either.right(new Combination(newCombination));
        levelSupplier.get().getBlockEntity(pos).setChanged(); // Save block entity
        return true;
    }


    // Helper

    private boolean hasItemsInInventory() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty())
                return true;
        }
        return false;
    }

    private ItemStackHandler createLockItemHandler() {
        return new ItemStackHandler(NonNullList.withSize(Combination.SIZE, ItemStack.EMPTY)) {
            @Override
            protected void onContentsChanged(int slot) {
                onLockInventoryChanged.accept(slot);
            }
        };
    }
}
