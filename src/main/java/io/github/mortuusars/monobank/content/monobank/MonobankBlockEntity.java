package io.github.mortuusars.monobank.content.monobank;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.monobank.content.monobank.inventory.IInventoryChangeListener;
import io.github.mortuusars.monobank.content.monobank.inventory.MonobankItemStackHandler;
import io.github.mortuusars.monobank.registry.ModBlockEntityTypes;
import io.github.mortuusars.monobank.registry.Registry;
import io.github.mortuusars.monobank.util.TextUtil;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

@SuppressWarnings({"NullableProblems", "unused"})
public class MonobankBlockEntity extends BlockEntity implements IInventoryChangeListener, MenuProvider, Nameable, LidBlockEntity {

    private static final String INVENTORY_KEY = "Inventory";
    private static final String LOCKED_KEY = "Locked";
    private static final String OWNER_KEY = "Owner";
    private static final String CUSTOM_NAME_KEY = "CustomName";

    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            MonobankBlockEntity.playSound(level, pos, state, Registry.Sounds.MONOBANK_OPEN.get());
        }

        protected void onClose(Level level, BlockPos pos, BlockState state) {
            MonobankBlockEntity.playSound(level, pos, state, Registry.Sounds.MONOBANK_CLOSE.get());
        }

        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int eventId, int eventParam) {
            MonobankBlockEntity.this.doorController.shouldBeOpen(MonobankBlockEntity.this.openersCounter.getOpenerCount() > 0);
        }

        protected boolean isOwnContainer(Player player) {
            return player.containerMenu instanceof MonobankMenu monobankMenu && monobankMenu.getBlockEntity() == MonobankBlockEntity.this;
        }
    };

    private final DoorOpennessController doorController = new DoorOpennessController(0.5f,
            0.35f, 0.6f, 0.6f, 0.36f);

    private Component customName;

    private final MonobankItemStackHandler inventory;
    private final LazyOptional<IItemHandler> inventoryHandler;

    private @Nullable UUID ownerUuid;
    private boolean locked = false;
    private boolean isUnlocking = false;
    private int unlockingCountdown = 0;
    private int unlockingCountdownMax = 0; // Used to calculate frequency of clicks when unlocking.

    public MonobankBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntityTypes.MONOBANK.get(), pPos, pBlockState);
        this.inventory = new MonobankItemStackHandler(this, 1);
        this.inventoryHandler = LazyOptional.of(() -> inventory);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(INVENTORY_KEY, inventory.serializeNBT());
        tag.putBoolean(LOCKED_KEY, locked);

        LogUtils.getLogger().info("Owner: " + ownerUuid);

        getOwnerUuid().ifPresent(uuid ->
                tag.putUUID(OWNER_KEY, uuid));
        if (this.customName != null)
            tag.putString(CUSTOM_NAME_KEY, Component.Serializer.toJson(this.customName));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound(INVENTORY_KEY));

        // cannot set the field directly here - we need to update DoorController to be in sync with client
        setLocked(tag.getBoolean(LOCKED_KEY));

        if (tag.contains(OWNER_KEY))
            ownerUuid = tag.getUUID(OWNER_KEY);
        if (tag.contains(CUSTOM_NAME_KEY, 8))
            this.customName = Component.Serializer.fromJson(tag.getString(CUSTOM_NAME_KEY));
    }


    // Ownership:

    public Optional<UUID> getOwnerUuid() {
        return Optional.ofNullable(ownerUuid);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasOwner() {
        return ownerUuid != null && !ownerUuid.equals(Util.NIL_UUID);
    }

    public boolean isOwnedBy(Player player) {
        return getOwnerUuid().orElse(Util.NIL_UUID).equals(player.getUUID());
    }

    public void setOwner(Player player) {
        if (!hasOwner())
            this.ownerUuid = player.getUUID();
        else
            throw new IllegalStateException("Cannot set owner. This Monobank already has an owner.");
    }

    // Locking

    public boolean isLocked() {
        return locked;
    }

    public boolean isUnlocking() {
        return isLocked() && isUnlocking/* && unlockingCountdown > 0*/;
    }

    /**
     * Starts the countdown after which Monobank will unlock.
     */
    public void startUnlocking() {
        startUnlocking(30);
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

    public void lock() {
        setLocked(true);
    }

    public void unlock() {
        setLocked(false);
    }

    private void setLocked(boolean locked) {
        this.locked = locked;
        doorController.setLocked(locked);
        isUnlocking = false;
        unlockingCountdown = 0;
        if (level != null && !level.isClientSide) { // Level is null when world loading, idk why.
            SoundEvent sound = locked ? Registry.Sounds.MONOBANK_LOCK.get() : Registry.Sounds.MONOBANK_UNLOCK.get();
            playSound(level, worldPosition, getBlockState(), sound, 1f);
            // We need to update clients with new state (otherwise door open/closing will not render):
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL); // Sync client.
        }
    }

    public ItemStack getStoredItemStack() {
        return inventory.getStackInSlot(0);
    }

//    public boolean isEmpty() {
//        for (int i = 0; i < inventory.getSlots(); i++) {
//            if (!inventory.getStackInSlot(i).isEmpty())
//                return false;
//        }
//        return true;
//    }

//    public boolean canDeposit(ItemStack stackToDeposit) {
//        if (isEmpty())
//            return true;
//
//        return inventory.isItemValid(0, stackToDeposit);
//    }


//    public ItemStack tryDepositItemStack(ItemStack stackToAdd) {
//
//        if (!canDeposit(stackToAdd))
//            return stackToAdd;
//
//        return inventory.insertItem(0, stackToAdd, false);
//    }

    /**
     * Creates new ItemStack from stored item with count up to maxStackSize or stored amount (whichever is smaller). If bank is empty - ItemStack.EMPTY is returned.
     */
//    public ItemStack withdrawStack() {
//        return withdraw(Item.MAX_STACK_SIZE);
//    }

    /**
     * Creates new ItemStack from stored item with specified count but not larger than maxStackSize or stored amount (whichever is smaller). If bank is empty - ItemStack.EMPTY is returned.
     */
//    public ItemStack withdraw(int count) {
//        if (count <= 0)
//            throw new IllegalArgumentException("'count' cannot be less than 1. Count: '" + count + "'.");
//
//        if (isEmpty())
//            return ItemStack.EMPTY;
//
//        return inventory.extractItem(0, count, false);
//    }



    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerID, Inventory playerInventory, Player player) {
        return new MonobankMenu(containerID, playerInventory, this);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return !this.remove && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? this.inventoryHandler.cast()
                : super.getCapability(cap, side);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        inventoryHandler.invalidate();
    }

    public static void playSound(Level level, BlockPos pos, BlockState state, SoundEvent sound) {
        playSound(level, pos, state, sound, 0.8F, level.random.nextFloat() * 0.1F + 0.9F);
    }

    public static void playSound(Level level, BlockPos pos, BlockState state, SoundEvent sound, float volume) {
        playSound(level, pos, state, sound, volume, level.random.nextFloat() * 0.05F + 0.95F);
    }

    public static void playSound(Level level, BlockPos pos, BlockState state, SoundEvent sound, float volume, float pitch) {
        // Offset sound source to door pos:
        Vec3i facingNormal = state.getValue(MonobankBlock.FACING).getNormal();
        double x = pos.getX() + 0.5D + (facingNormal.getX() * 0.5D);
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D + (facingNormal.getZ() * 0.5D);

        level.playSound(null, x, y, z, sound, SoundSource.BLOCKS, volume, pitch);
    }
    
    public static <T extends BlockEntity> void clientTick(Level level, BlockPos blockPos, BlockState blockState, T blockEntity) {
        if (blockEntity instanceof MonobankBlockEntity monobankEntity)
            monobankEntity.doorController.tickDoor();
    }

    public static <T extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, T blockEntity) {
        if (blockEntity instanceof MonobankBlockEntity monobankEntity) {
            if (monobankEntity.unlockingCountdown > 0) {
                // Calculating frequency of clicks (closer to unlocking -> more time between clicks):
                int countdown = monobankEntity.unlockingCountdown;
                int countdownMax = monobankEntity.unlockingCountdownMax;
                int max = (int)Math.ceil(Math.log(countdownMax)) + 1;
                int current = (int)Math.ceil(Math.log(countdown));
                int freq = max - current;
                if (countdown % freq == 0)
                    playSound(level, blockPos, blockState, Registry.Sounds.MONOBANK_CLICK.get(), 0.5f);

                monobankEntity.unlockingCountdown--;
            }
            else if (monobankEntity.isUnlocking() && monobankEntity.unlockingCountdown <= 0)
                monobankEntity.unlock();
        }
    }


    // Openers Counter

    public void startOpen(Player player) {
        if (level != null && !this.remove && !player.isSpectator()) {
            this.openersCounter.incrementOpeners(player, level, this.getBlockPos(), this.getBlockState());
        }
    }

    public void stopOpen(Player player) {
        if (level != null && !this.remove && !player.isSpectator()) {
            this.openersCounter.decrementOpeners(player, level, this.getBlockPos(), this.getBlockState());
        }
    }

    public void recheckOpen() {
        if (level != null && !this.remove) {
            this.openersCounter.recheckOpeners(level, this.getBlockPos(), this.getBlockState());
        }
    }

    public float getOpenNess(float partialTicks) {
        return this.doorController.getOpenness(partialTicks);
    }

    public static int getOpenCount(BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof MonobankBlockEntity monobankEntity)
            return monobankEntity.openersCounter.getOpenerCount();

        return 0;
    }


    // Name
    @Override
    public @NotNull Component getDisplayName() {
        return getName();
    }

    public @NotNull Component getName() {
        return this.customName != null ? this.customName : TextUtil.translate("monobank");
    }

    public void setCustomName(Component customName) {
        this.customName = customName;
    }


    // Sync
    @Override
    @javax.annotation.Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        @Nullable CompoundTag tag = pkt.getTag();
        if (tag == null) // Sometimes it is null. IDK.
            tag = new CompoundTag();
        load(tag);
    }

    public void inventoryChanged(int changedSlot) {
        super.setChanged();
        if (level != null)
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }
}
