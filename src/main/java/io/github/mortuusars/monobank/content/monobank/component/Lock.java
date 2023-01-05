package io.github.mortuusars.monobank.content.monobank.component;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.monobank.Registry;
import io.github.mortuusars.monobank.content.monobank.MonobankBlockEntity;
import io.github.mortuusars.monobank.content.monobank.unlocking.Combination;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages Monobank locking and unlocking.
 */
public class Lock {
    private static final String LOCKED_NBT_KEY = "Locked";
    private static final String COMBINATION_TABLE_NBT_KEY = "CombinationTable";
    private static final String COMBINATION_NBT_KEY = "Combination";
    private static final String INVENTORY_NBT_KEY = "LockInventory";

    public static final int SIZE = 3;

    // Serialized fields:
    private @Nullable ResourceLocation combinationTable;
    private Combination combination;
    private final ItemStackHandler inventory;
    private boolean locked = false;

    private boolean isUnlocking = false;
    private int unlockingCountdown = 0;
    private int unlockingCountdownMax = 0; // Used to calculate frequency of clicks when unlocking.

    private MonobankBlockEntity monobankEntity;
    private Runnable onLockedChanged;

    public Lock(MonobankBlockEntity monobankEntity, Runnable onLockedChanged) {
        this.monobankEntity = monobankEntity;
        this.onLockedChanged = onLockedChanged;
        this.combination = Combination.empty();
        this.inventory = createLockItemHandler();
    }

    public IItemHandler getInventoryHandler() {
        return inventory;
    }

    public void setCombination(Item first, Item second, Item third) {
        this.combination = new Combination(first, second, third);
    }

    public Combination getCombination() {
        return combination;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isUnlocking() {
        return isUnlocking;
    }

    /**
     * Starts the countdown after which Monobank will unlock.
     */
    public void startUnlocking() {
        startUnlocking(monobankEntity.getLevel().getRandom().nextInt(20, 61));
    }

    /**
     * Starts the countdown for specified amount of ticks after which Monobank will unlock.
     */
    public void startUnlocking(int ticks) {
        if (!isUnlocking()) {
            isUnlocking = true;
            unlockingCountdown = ticks;
            unlockingCountdownMax = ticks;
        }
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        isUnlocking = false;
        unlockingCountdown = 0;
        onLockedChanged.run();
    }

    public void tick() {
        if (unlockingCountdown > 0) {
            // Calculating frequency of clicks (closer to unlocking -> more time between clicks):
            int max = (int)Math.ceil(Math.log(unlockingCountdownMax)) + 1;
            int current = (int)Math.ceil(Math.log(unlockingCountdown));
            int freq = max - current;
            if (unlockingCountdown % freq == 0)
                monobankEntity.playSoundAtDoor(monobankEntity.getLevel(), monobankEntity.getBlockPos(),
                        monobankEntity.getBlockState(), Registry.Sounds.MONOBANK_CLICK.get(), 0.5f);

            unlockingCountdown--;
        }
        else if (isUnlocking() && unlockingCountdown <= 0)
            setLocked(false);
    }


    private void onInventoryChanged(int slot) {
        monobankEntity.inventoryChanged();

        if (combination.matches(slot, inventory.getStackInSlot(slot)))
            monobankEntity.playSoundAtDoor(Registry.Sounds.MONOBANK_CLICK.get());

        List<ItemStack> keys = new ArrayList<>();
        for (int i = 0; i < inventory.getSlots(); i++) {
            keys.add(inventory.getStackInSlot(i));
        }

        if (combination.matches(keys)) {
            this.startUnlocking();
            monobankEntity.dropItemsAtDoor(keys);
        }
    }


    // Save

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(LOCKED_NBT_KEY, locked);

        if (combinationTable != null)
            tag.putString(COMBINATION_TABLE_NBT_KEY, combinationTable.toString());
        else if (!combination.isEmpty())
            tag.put(COMBINATION_NBT_KEY, combination.serializeNBT());

        if (hasItemsInInventory())
            tag.put(INVENTORY_NBT_KEY, inventory.serializeNBT());
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag.isEmpty())
            return;

        locked = tag.getBoolean(LOCKED_NBT_KEY);

        if (tag.contains(COMBINATION_TABLE_NBT_KEY, CompoundTag.TAG_STRING)) {
            combinationTable = new ResourceLocation(tag.getString(COMBINATION_TABLE_NBT_KEY));
            unpackCombinationTable();
        }
        else if (tag.contains(COMBINATION_NBT_KEY, CompoundTag.TAG_COMPOUND))
            combination.deserializeNBT(tag.getCompound(COMBINATION_NBT_KEY));

        if (tag.contains(INVENTORY_NBT_KEY))
            inventory.deserializeNBT(tag.getCompound(INVENTORY_NBT_KEY));
    }

    private void unpackCombinationTable() {
        Level level = monobankEntity.getLevel();
        if (this.combinationTable != null && level.getServer() != null) {
            LootTable loottable = level.getServer().getLootTables().get(this.combinationTable);
            this.combinationTable = null;
            LootContext.Builder lootContextBuilder = (new LootContext.Builder((ServerLevel)level))
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(monobankEntity.getBlockPos()));

            List<ItemStack> randomItems = loottable.getRandomItems(lootContextBuilder.create(LootContextParamSets.CHEST));

            ArrayList<ItemStack> newCombination = new ArrayList<>();

            for (int i = 0; i < 3; i++) {
                if (randomItems.size() < i)
                    newCombination.add(ItemStack.EMPTY);
                else
                    newCombination.add(randomItems.get(i));
            }

            Collections.shuffle(newCombination);

            this.combination = new Combination(newCombination);
            monobankEntity.setChanged();
        }
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
        return new ItemStackHandler(NonNullList.withSize(SIZE, ItemStack.EMPTY)) {
            @Override
            protected void onContentsChanged(int slot) {
                onInventoryChanged(slot);
            }
        };
    }
}
