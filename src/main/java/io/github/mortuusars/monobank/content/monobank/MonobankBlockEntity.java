package io.github.mortuusars.monobank.content.monobank;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.Registry;
import io.github.mortuusars.monobank.content.monobank.component.DoorOpennessController;
import io.github.mortuusars.monobank.content.monobank.component.Lock;
import io.github.mortuusars.monobank.content.monobank.component.Owner;
import io.github.mortuusars.monobank.content.monobank.unlocking.MonobankUnlockingMenu;
import io.github.mortuusars.monobank.core.base.SyncedBlockEntity;
import io.github.mortuusars.monobank.core.inventory.MonobankItemStackHandler;
import io.github.mortuusars.monobank.util.TextUtil;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings({"NullableProblems", "unused"})
public class MonobankBlockEntity extends SyncedBlockEntity implements MenuProvider, Nameable, LidBlockEntity {
    private static final String INVENTORY_NBT_KEY = "Inventory";
    private static final String LOCK_NBT_KEY = "Lock";
    private static final String OWNER_NBT_KEY = "Owner";
    private static final String CUSTOM_NAME_NBT_KEY = "CustomName";

    private static final int UPDATE_DOOR_EVENT_ID = 1;

    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            MonobankBlockEntity.playSoundAtDoor(level, pos, state, Registry.Sounds.MONOBANK_OPEN.get());
        }

        protected void onClose(Level level, BlockPos pos, BlockState state) {
            MonobankBlockEntity.playSoundAtDoor(level, pos, state, Registry.Sounds.MONOBANK_CLOSE.get());
        }

        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int eventId, int eventParam) {
            // Send update to client:
            level.blockEvent(pos, state.getBlock(), UPDATE_DOOR_EVENT_ID, eventParam);
        }

        protected boolean isOwnContainer(Player player) {
            return player.containerMenu instanceof MonobankMenu monobankMenu
                    && monobankMenu.getBlockEntity() == MonobankBlockEntity.this;
        }
    };
    private final DoorOpennessController doorOpennessController;

    private final MonobankItemStackHandler inventory;
    private final LazyOptional<IItemHandler> inventoryHandler;
    private final Lock lock;
    private Owner owner;
    private Component customName;

    private final MenuProvider OPEN_MENU_PROVIDER = new MenuProvider() {
        @Override
        public Component getDisplayName() {
            return MonobankBlockEntity.this.getName();
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerID, Inventory playerInventory, Player player) {
            return new MonobankMenu(containerID, playerInventory, MonobankBlockEntity.this);
        }
    };

    private final MenuProvider UNLOCKING_MENU_PROVIDER = new MenuProvider() {
        @Override
        public Component getDisplayName() {
            return TextUtil.translate("gui.unlocking", MonobankBlockEntity.this.getName());
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerID, Inventory playerInventory, Player player) {
            return new MonobankUnlockingMenu(containerID, playerInventory, MonobankBlockEntity.this,
                    MonobankBlockEntity.this.lock.getCombination());
        }
    };

    private float fullness = -1;

    public static final String LOOT_TABLE_TAG = "LootTable";
    public static final String LOOT_TABLE_SEED_TAG = "LootTableSeed";
    @Nullable
    protected ResourceLocation lootTable;
    protected long lootTableSeed;

    public MonobankBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(Registry.BlockEntityTypes.MONOBANK.get(), pPos, pBlockState);
        this.inventory = new MonobankItemStackHandler(slot -> inventoryChanged(), 1);
        this.inventoryHandler = LazyOptional.of(() -> inventory);
        doorOpennessController = new DoorOpennessController(0.5f,
                0.35f, 0.6f, 0.65f, 0.36f);
        lock = new Lock(this, this::onLockedChanged);
        owner = Owner.none();
    }


    // Lock

    public Lock getLock() {
        return lock;
    }

    private void onLockedChanged() {
        boolean isLocked = lock.isLocked();
        LogUtils.getLogger().warn(isLocked + " ");
        doorOpennessController.setLocked(isLocked);

        if (level != null && !level.isClientSide) { // Level is null when world loading, idk why.
            SoundEvent sound = isLocked ? Registry.Sounds.MONOBANK_LOCK.get() : Registry.Sounds.MONOBANK_UNLOCK.get();
            playSoundAtDoor(level, worldPosition, getBlockState(), sound, 1f);
            // We need to update clients with new state (otherwise door open/closing will not render):
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS); // Sync client.
        }
    }


    // Save/Load

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!trySaveLootTable(tag))
            tag.put(INVENTORY_NBT_KEY, inventory.serializeNBT());
        tag.put(LOCK_NBT_KEY, lock.serializeNBT());
        if (owner.getType() != Owner.Type.NONE)
            tag.put(OWNER_NBT_KEY, owner.serializeNBT());
        if (this.customName != null)
            tag.putString(CUSTOM_NAME_NBT_KEY, Component.Serializer.toJson(this.customName));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (!tryLoadLootTable(tag))
            inventory.deserializeNBT(tag.getCompound(INVENTORY_NBT_KEY));
        lock.deserializeNBT(tag.getCompound(LOCK_NBT_KEY));
        if (tag.contains(OWNER_NBT_KEY, CompoundTag.TAG_COMPOUND))
            owner.deserializeNBT(tag.getCompound(OWNER_NBT_KEY));
        if (tag.contains(CUSTOM_NAME_NBT_KEY, CompoundTag.TAG_STRING))
            this.customName = Component.Serializer.fromJson(tag.getString(CUSTOM_NAME_NBT_KEY));
        updateFullness();
        doorOpennessController.setLocked(lock.isLocked());
    }

    protected boolean tryLoadLootTable(CompoundTag tag) {
        if (tag.contains("LootTable", 8)) {
            this.lootTable = new ResourceLocation(tag.getString("LootTable"));
            this.lootTableSeed = tag.getLong("LootTableSeed");
            return true;
        } else {
            return false;
        }
    }

    protected boolean trySaveLootTable(CompoundTag tag) {
        if (this.lootTable == null) {
            return false;
        } else {
            tag.putString("LootTable", this.lootTable.toString());
            if (this.lootTableSeed != 0L) {
                tag.putLong("LootTableSeed", this.lootTableSeed);
            }

            return true;
        }
    }

    public void unpackLootTable(@Nullable Player player) {
        if (this.lootTable != null && this.level.getServer() != null) {
            if (!this.inventory.getStackInSlot(0).isEmpty()) {
                LogUtils.getLogger().warn("Tried to unpack Loot Table while Monobank is not empty. Loot Table will not be unpacked.");
                return;
            }

            LootTable loottable = this.level.getServer().getLootTables().get(this.lootTable);
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)player, this.lootTable);
            }

            this.lootTable = null;
            LootContext.Builder lootContextBuilder = (new LootContext.Builder((ServerLevel)this.level)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.worldPosition)).withOptionalRandomSeed(this.lootTableSeed);
            if (player != null) {
                lootContextBuilder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
            }


            List<ItemStack> randomItems = loottable.getRandomItems(lootContextBuilder.create(LootContextParamSets.CHEST));
            if (randomItems.size() > 0) {
                this.inventory.setStackInSlot(0, randomItems.get(0));
            }
        }
    }

    // Ownership:

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

    public void onSetPlacedBy(LivingEntity placer, ItemStack stack) {
        if (stack.getOrCreateTag().contains("BlockEntityTag", CompoundTag.TAG_COMPOUND)) {
            CompoundTag blockEntityTag = stack.getTag().getCompound("BlockEntityTag");
            if (blockEntityTag.contains("Owner")){
                Owner owner = Owner.none();
                owner.deserializeNBT(blockEntityTag.getCompound("Owner"));
                if (owner.getType() != Owner.Type.NONE)
                    return; // Do not set new owner if bank already has one.
            }
        }

        if (placer instanceof Player player)
            setOwner(player);
    }

    // GUI

    public void openUnlockingGui(ServerPlayer player) {
        if (lock.isLocked()) {

            if (lock.getCombination().isEmpty()) {
                lock.setCombination(Items.AIR, Items.AIR, Items.IRON_NUGGET);
            }

            NetworkHooks.openGui(player, this.UNLOCKING_MENU_PROVIDER, buffer -> {
                buffer.writeBlockPos(worldPosition);
                lock.getCombination().toBuffer(buffer);
            });
        }
    }

    public void open(ServerPlayer player) {
        NetworkHooks.openGui(player, this.OPEN_MENU_PROVIDER, worldPosition);
    }



    // Inventory

    public IItemHandler getUnlockingInventoryHandler() {
        return lock.getInventoryHandler();
    }

    public float getFullness() {
        return fullness;
    }

    public void updateFullness() {
        fullness = Mth.clamp(getStoredItemStack().getCount() / (float)Monobank.getSlotCapacity(), 0.0f, 1.0f);
    }

    public ItemStack getStoredItemStack() {
        return inventory.getStackInSlot(0);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        unpackLootTable(null);
        return !this.remove && !lock.isLocked() && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ?
                this.inventoryHandler.cast() :
                super.getCapability(cap, side);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerID, Inventory playerInventory, Player player) {
        return new MonobankMenu(containerID, playerInventory, this);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        inventoryHandler.invalidate();
    }

    public void inventoryChanged() {
        this.setChanged();
        updateFullness();
        // Advancement
        if (!level.isClientSide && getOwner().isPlayerOwned()) {
            @Nullable ServerPlayer player = level.getServer().getPlayerList().getPlayer(getOwner().getUuid());
            if (player != null)
                Registry.Advancements.MONOBANK_ITEMS_COUNT.trigger(player, getStoredItemStack().getCount());
        }
    }

    public boolean triggerEvent(int id, int param) {
        if (id == UPDATE_DOOR_EVENT_ID) {
            this.doorOpennessController.shouldBeOpen(param > 0);
            return true;
        } else {
            return super.triggerEvent(id, param);
        }
    }


    // Tick

    public static <T extends BlockEntity> void clientTick(Level level, BlockPos blockPos, BlockState blockState, T blockEntity) {
        if (blockEntity instanceof MonobankBlockEntity monobankEntity)
            monobankEntity.doorOpennessController.tickDoor();
    }

    public static <T extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, T blockEntity) {
        if (blockEntity instanceof MonobankBlockEntity monobankEntity) {
            if (!monobankEntity.getLock().isLocked())
                monobankEntity.unpackLootTable(null);
            monobankEntity.lock.tick();
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
        return this.doorOpennessController.getOpenness(partialTicks);
    }


    // Name
    @Override
    public @NotNull Component getDisplayName() {
        return getName();
    }
    public @NotNull Component getName() {
        return this.customName != null ? this.customName : TextUtil.translate("gui.monobank");
    }
    public void setCustomName(Component customName) {
        this.customName = customName;
    }


    // Helpers

    public void dropItemsAtDoor(List<ItemStack> items) {
        Vec3i facingNormal = getBlockState().getValue(MonobankBlock.FACING).getNormal();
        double x = worldPosition.getX() + 0.5D + (facingNormal.getX() * 0.6D);
        double y = worldPosition.getY() + 0.5D;
        double z = worldPosition.getZ() + 0.5D + (facingNormal.getZ() * 0.6D);
        for (ItemStack stack : items) {
            Containers.dropItemStack(level, x, y, z, stack);
        }
    }

    public void playSoundAtDoor(SoundEvent sound, float volume, float pitch) {
        playSoundAtDoor(level, worldPosition, getBlockState(), sound, volume, pitch);
    }

    public void playSoundAtDoor(SoundEvent sound) {
        playSoundAtDoor(level, worldPosition, getBlockState(), sound);
    }

    public static void playSoundAtDoor(Level level, BlockPos pos, BlockState state, SoundEvent sound) {
        playSoundAtDoor(level, pos, state, sound, 1F, level.random.nextFloat() * 0.1F + 0.9F);
    }

    public static void playSoundAtDoor(Level level, BlockPos pos, BlockState state, SoundEvent sound, float volume) {
        playSoundAtDoor(level, pos, state, sound, volume, level.random.nextFloat() * 0.1F + 0.9F);
    }

    public static void playSoundAtDoor(Level level, BlockPos pos, BlockState state, SoundEvent sound, float volume, float pitch) {
        // Offset sound source to door pos:
        Vec3i facingNormal = state.getValue(MonobankBlock.FACING).getNormal();
        double x = pos.getX() + 0.5D + (facingNormal.getX() * 0.5D);
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D + (facingNormal.getZ() * 0.5D);

        level.playSound(null, x, y, z, sound, SoundSource.BLOCKS, volume, pitch);
    }
}
