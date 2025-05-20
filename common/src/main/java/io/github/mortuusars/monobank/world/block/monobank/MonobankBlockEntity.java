package io.github.mortuusars.monobank.world.block.monobank;

import com.google.common.base.Preconditions;
import io.github.mortuusars.monobank.Config;
import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.PlatformHelper;
import io.github.mortuusars.monobank.world.inventory.menu.MonobankMenu;
import io.github.mortuusars.monobank.world.block.monobank.component.Combination;
import io.github.mortuusars.monobank.world.inventory.menu.CombinationMenu;
import io.github.mortuusars.monobank.world.block.monobank.component.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MonobankBlockEntity extends BlockEntity implements Nameable, LidBlockEntity, RandomizableContainer {
    public static final String ITEM_TAG = "Item";
    public static final String ITEM_COUNT_TAG = "ItemCount";
    public static final String LOCK_TAG = "Lock";
    public static final String OWNER_TAG = "Owner";
    public static final String WARNINGS_SEEN_COUNT_TAG = "WarningsSeenCount";
    public static final String BREAK_IN_SUCCEEDED_TAG = "BreakInSucceeded";
    public static final String BREAK_IN_ATTEMPTED_TAG = "BreakInAttempted";
    public static final String CUSTOM_NAME_TAG = "CustomName";

    protected static final int UPDATE_DOOR_EVENT_ID = 1;

    protected final ContainerOpenersCounter openersCounter = new MonobankOpenersCounter(this);
    protected final DoorOpennessController doorOpennessController = new DoorOpennessController(0.5f,
            0.35f, 0.6f, 0.65f, 0.36f);

    protected ItemStack item;

    protected @Nullable ResourceKey<LootTable> lootTable;
    protected long lootTableSeed = 0L;

    protected final Lock lock;
    protected Owner owner;
    protected int warningsSeenCount;
    protected boolean breakInAttempted, breakInSucceeded;
    protected @Nullable Component customName;

    protected float fullness = -1;

    public MonobankBlockEntity(BlockPos pos, BlockState state) {
        super(Monobank.BlockEntityTypes.MONOBANK.get(), pos, state);
        this.item = ItemStack.EMPTY;
        this.lock = new Lock(this::onLockedChanged);
        this.owner = Owner.none();
    }

    // -- Tick

    public static <T extends BlockEntity> void clientTick(Level level, BlockPos blockPos, BlockState blockState, T blockEntity) {
        if (blockEntity instanceof MonobankBlockEntity monobankEntity) {
            monobankEntity.doorOpennessController.tickDoor();
        }
    }

    public static <T extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, T blockEntity) {
        if (level instanceof ServerLevel serverLevel && blockEntity instanceof MonobankBlockEntity monobankEntity) {
            monobankEntity.tick(serverLevel);
        }
    }

    public void tick(ServerLevel serverLevel) {
        getLock().tick(serverLevel, this);

        if (!getLock().isLocked() && getLootTable() != null) {
            unpackLootTable(null);
            setChanged();
        }
    }

    // --

    public int getCapacity() {
        return Config.Server.MONOBANK_CAPACITY.get();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean canReplaceLock(Player player) {
        if (getLock().isLocked()) return false;
        if (!getOwner().isPlayerOwned()) return true;
        if (getOwner().isOwnedBy(player)) return true;
        return Config.Server.CAN_REPLACE_OTHER_PLAYERS_LOCKS.get();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    // -- Lock

    public Lock getLock() {
        return lock;
    }

    public boolean replaceLock(Player player, Combination combination) {
        if (!canReplaceLock(player)) return false;

        getLock().setCombination(combination);
        setOwner(player);
        playSoundAtDoor(Monobank.SoundEvents.MONOBANK_CLICK.get()); // TODO: Lock Replacement Sound

        if (player instanceof ServerPlayer serverPlayer) {
            Monobank.CriteriaTriggers.MONOBANK_LOCK_REPLACED.get().trigger(serverPlayer, this);
        }

        setChanged();

        return true;
    }

    /**
     * Starts the countdown after which Monobank will unlock.
     */
    public void startUnlocking(Player player) {
        if (level == null) return;
        startUnlocking(player, level.getRandom().nextInt(20, 61));
    }

    /**
     * Starts the countdown for specified amount of ticks after which Monobank will unlock.
     */
    public void startUnlocking(Player player, int ticks) {
        if (getLock().isUnlocking()) return;

        // TODO: Thief commit crime
        // checkAndPunishForCrime(player, Thief.Offence.HEAVY);

        if (getOwner().isPlayerOwned() && !getOwner().isOwnedBy(player)) {
            breakInSucceeded = true;
        }

        getLock().startUnlocking(ticks);

        setChanged();
    }

    protected void onLockedChanged() {
        boolean isLocked = getLock().isLocked();
        doorOpennessController.setLocked(isLocked);

        if (level != null && !level.isClientSide) {
            SoundEvent sound = isLocked ? Monobank.SoundEvents.MONOBANK_LOCK.get() : Monobank.SoundEvents.MONOBANK_UNLOCK.get();
            playSoundAtDoor(sound);
        }

        setChanged();
    }

    // -- Ownership

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
        setChanged();
    }

    public void setOwner(Player player) {
        setOwner(new Owner(player));
    }

    public void onSetPlacedBy(@Nullable LivingEntity placer, ItemStack stack) {
        if (placer instanceof Player player && !player.level().isClientSide) {
            if (getOwner().getType() == Owner.Type.NONE) {
                setOwner(player);
            }

            if (!getLock().hasCombination()) {
                getLock().setCombinationTable(Monobank.LootTables.COMBINATION_DEFAULT);
            }

            setChanged();
        }
    }

    // -- GUI

    public void openUnlockingGui(ServerPlayer player) {
        if (!getLock().isLocked()) {
            return;
        }

        getLock().unpackCombinationTableIfNeeded(player.serverLevel(), getBlockPos());

        //TODO: Thief commit crime
        //checkAndPunishForCrime(player, Thief.Offence.LIGHT);

        if (getLock().getCombination().isEmpty()) {
            startUnlocking(player);
        } else {
            if (getOwner().isPlayerOwned() && !getOwner().isOwnedBy(player)) {
                breakInAttempted = true;
                setChanged();
            }
            PlatformHelper.openMenu(player, new MenuProvider() {
                @Override
                public @NotNull Component getDisplayName() {
                    return Component.translatable("monobank.gui.monobank.unlocking", MonobankBlockEntity.this.getName());
                }

                @Override
                public @NotNull AbstractContainerMenu createMenu(int containerID, Inventory playerInventory, Player player1) {
                    return new CombinationMenu(containerID, playerInventory, MonobankBlockEntity.this,
                            MonobankBlockEntity.this.getLock().getCombination());
                }
            }, buffer -> {
                buffer.writeBlockPos(worldPosition);
                getLock().getCombination().toBuffer(buffer);
            });
        }
    }

    public void open(ServerPlayer player) {
        //TODO: Thief commit crime
        //checkAndPunishForCrime(player, Thief.Offence.MODERATE);

        PlatformHelper.openMenu(player, new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return MonobankBlockEntity.this.getName();
            }

            @Override
            public @NotNull AbstractContainerMenu createMenu(int containerID, Inventory playerInventory, Player player1) {
                return new MonobankMenu(containerID, playerInventory, MonobankBlockEntity.this,
                        MonobankBlockEntity.this.getExtraInfo(player1));
            }
        }, buffer -> {
            buffer.writeBlockPos(worldPosition);
            getExtraInfo(player).toBuffer(buffer);
        });
    }

    /**
     * Used to provide gui with extra info.
     */
    public MonobankExtraInfo getExtraInfo(Player player) {
        return new MonobankExtraInfo(getOwner().isOwnedBy(player), breakInAttempted, breakInSucceeded);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }

    public void inventoryChanged() {
        updateFullness();
        setChanged();
        // Advancement
        if (level != null && !level.isClientSide && level.getServer() != null && getOwner().isPlayerOwned()) {
            @Nullable ServerPlayer player = level.getServer().getPlayerList().getPlayer(getOwner().getUuid());
            if (player != null) {
                Monobank.CriteriaTriggers.MONOBANK_INVENTORY_CHANGED.get().trigger(player, this, getItem());
            }
        }
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == UPDATE_DOOR_EVENT_ID) {
            this.doorOpennessController.shouldBeOpen(param > 0);
            return true;
        } else {
            return super.triggerEvent(id, param);
        }
    }

    // -- Openers Counter

    public void startOpen(Player player) {
        if (level != null && !this.remove && !player.isSpectator()) {
            this.openersCounter.incrementOpeners(player, level, this.getBlockPos(), this.getBlockState());
        }
    }

    public void stopOpen(Player player) {
        if (level != null && !this.remove && !player.isSpectator()) {
            this.openersCounter.decrementOpeners(player, level, this.getBlockPos(), this.getBlockState());

            if (getOwner().isOwnedBy(player)) {
                if (breakInAttempted || breakInSucceeded) {
                    warningsSeenCount++;
                }

                if (warningsSeenCount >= 3) {
                    breakInAttempted = false;
                    breakInSucceeded = false;
                    warningsSeenCount = 0;
                }
            }
        }
    }

    public void recheckOpen() {
        if (level != null && !this.remove) {
            this.openersCounter.recheckOpeners(level, this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public float getOpenNess(float partialTicks) {
        return this.doorOpennessController.getOpenness(partialTicks);
    }

    // -- Name

    @Override
    public @NotNull Component getDisplayName() {
        return getName();
    }

    public @NotNull Component getName() {
        return this.customName != null ? this.customName : Component.translatable("monobank.gui.monobank");
    }

    public void setCustomName(@Nullable Component customName) {
        this.customName = customName;
    }

    // -- Inventory

    public ItemStack getItem() {
        return item;
    }

    public float getFullness() {
        return fullness;
    }

    public void updateFullness() {
        fullness = Mth.clamp(getItem().getCount() / (float) getCapacity(), 0.0f, 1.0f);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return getItem().isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        checkSlotIndex(slot);
        return item;
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        checkSlotIndex(slot);
        ItemStack stack = item.split(Math.min(item.getCount(), amount));

        if (item.isEmpty()) {
            item = ItemStack.EMPTY;
        }

        inventoryChanged();
        return stack;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        checkSlotIndex(slot);
        ItemStack stack = item.copy();
        item = ItemStack.EMPTY;
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        checkSlotIndex(slot);
        item = stack;
        inventoryChanged();
    }

    @Override
    public void clearContent() {
        item = ItemStack.EMPTY;
        inventoryChanged();
    }

    protected void checkSlotIndex(int slot) {
        Preconditions.checkElementIndex(slot, 1);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return !Config.Server.LOCK_PREVENTS_ITEM_INSERTION.get() || !getLock().isLocked();
    }

    @Override
    public boolean canTakeItem(Container target, int slot, ItemStack stack) {
        return !Config.Server.LOCK_PREVENTS_ITEM_EXTRACTION.get() || !getLock().isLocked();
    }

    // -- Loot Table

    @Override
    public @Nullable ResourceKey<LootTable> getLootTable() {
        return lootTable;
    }

    @Override
    public void setLootTable(@Nullable ResourceKey<LootTable> lootTable) {
        this.lootTable = lootTable;
    }

    @Override
    public long getLootTableSeed() {
        return lootTableSeed;
    }

    @Override
    public void setLootTableSeed(long seed) {
        this.lootTableSeed = seed;
    }

    // -- Save / Load

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        if (level != null) {
            components.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(saveWithId(level.registryAccess())));
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!trySaveLootTable(tag) && !item.isEmpty()) {
            ItemStack savedStack = item.copy();
            savedStack.setCount(1);
            Tag itemTag = savedStack.save(registries);

            tag.put(ITEM_TAG, itemTag);
            tag.putInt(ITEM_COUNT_TAG, item.getCount());
        }
        tag.put(LOCK_TAG, lock.save());
        if (owner.getType() != Owner.Type.NONE) {
            tag.put(OWNER_TAG, owner.serializeNBT());
        }
        if (this.customName != null) {
            tag.putString(CUSTOM_NAME_TAG, Component.Serializer.toJson(this.customName, registries));
        }

        if (getOwner().isPlayerOwned()) {
            if (warningsSeenCount > 0) {
                tag.putInt(WARNINGS_SEEN_COUNT_TAG, warningsSeenCount);
            }
            if (breakInAttempted) {
                tag.putBoolean(BREAK_IN_ATTEMPTED_TAG, true);
            }
            if (breakInSucceeded) {
                tag.putBoolean(BREAK_IN_SUCCEEDED_TAG, true);
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (!tryLoadLootTable(tag)) {
            CompoundTag itemTag = tag.getCompound(ITEM_TAG);
            if (itemTag.isEmpty()) {
                item = ItemStack.EMPTY;
            } else {
                ItemStack.parse(registries, itemTag).ifPresent(stack -> {
                    item = stack;
                    stack.setCount(tag.getInt(ITEM_COUNT_TAG));
                });
            }
        }
        lock.load(tag.getCompound(LOCK_TAG));
        if (tag.contains(OWNER_TAG, CompoundTag.TAG_COMPOUND)) {
            owner.deserializeNBT(tag.getCompound(OWNER_TAG));
        }
        if (tag.contains(CUSTOM_NAME_TAG, CompoundTag.TAG_STRING)) {
            this.customName = Component.Serializer.fromJson(tag.getString(CUSTOM_NAME_TAG), registries);
        }
        warningsSeenCount = tag.getInt(WARNINGS_SEEN_COUNT_TAG);
        breakInSucceeded = tag.getBoolean(BREAK_IN_SUCCEEDED_TAG);
        breakInAttempted = tag.getBoolean(BREAK_IN_ATTEMPTED_TAG);
        updateFullness();
        doorOpennessController.setLocked(lock.isLocked());
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // -- Sync

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    // -- Util

    public void dropItemsAtDoor(List<ItemStack> items) {
        Vec3i facingNormal = getBlockState().getValue(MonobankBlock.FACING).getNormal();
        double x = worldPosition.getX() + 0.5D + (facingNormal.getX() * 0.6D);
        double y = worldPosition.getY() + 0.5D;
        double z = worldPosition.getZ() + 0.5D + (facingNormal.getZ() * 0.6D);
        for (ItemStack stack : items) {
            assert level != null;
            Containers.dropItemStack(level, x, y, z, stack);
        }
    }

    public void playSoundAtDoor(SoundEvent sound) {
        playSoundAtDoor(sound, 1f, 1f);
    }

    public void playSoundAtDoor(SoundEvent sound, float volume, float pitch) {
        playSoundAtDoor(null, sound, volume, pitch);
    }

    public void playSoundAtDoor(@Nullable Player player, SoundEvent sound) {
        playSoundAtDoor(player, sound, 1f, 1f);
    }

    public void playSoundAtDoor(@Nullable Player player, SoundEvent sound, float volume, float pitch) {
        if (level == null) return;

        // Offset sound source to door pos:
        Vec3i facingNormal = getBlockState().getValue(MonobankBlock.FACING).getNormal();
        BlockPos pos = getBlockPos();
        double x = pos.getX() + 0.5D + (facingNormal.getX() * 0.5D);
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D + (facingNormal.getZ() * 0.5D);

        level.playSound(player, x, y, z, sound, SoundSource.BLOCKS, volume, pitch);
    }
}
