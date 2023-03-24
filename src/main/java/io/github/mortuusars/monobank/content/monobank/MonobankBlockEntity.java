package io.github.mortuusars.monobank.content.monobank;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.Registry;
import io.github.mortuusars.monobank.config.Configuration;
import io.github.mortuusars.monobank.Thief;
import io.github.mortuusars.monobank.content.monobank.component.*;
import io.github.mortuusars.monobank.content.monobank.unlocking.Combination;
import io.github.mortuusars.monobank.content.monobank.unlocking.UnlockingMenu;
import io.github.mortuusars.monobank.core.base.SyncedBlockEntity;
import io.github.mortuusars.monobank.core.inventory.MonobankItemStackHandler;
import io.github.mortuusars.monobank.util.TextUtil;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"NullableProblems", "unused"})
public class MonobankBlockEntity extends SyncedBlockEntity implements Nameable, LidBlockEntity {
    public static final String INVENTORY_TAG = "Inventory";
    public static final String LOCK_TAG = "Lock";
    public static final String OWNER_TAG = "Owner";
    public static final String CUSTOM_NAME_TAG = "CustomName";
    public static final String LOOT_TABLE_TAG = "LootTable";
    public static final String LOOT_TABLE_SEED_TAG = "LootTableSeed";
    public static final String BREAK_IN_SUCCEEDED_TAG = "BreakInSucceeded";
    public static final String BREAK_IN_ATTEMPTED_TAG = "BreakInAttempted";

    private static final int UPDATE_DOOR_EVENT_ID = 1;

    private final MenuProvider OPEN_MENU_PROVIDER = new MenuProvider() {
        @Override
        public Component getDisplayName() {
            return MonobankBlockEntity.this.getName();
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerID, Inventory playerInventory, Player player) {
            return new MonobankMenu(containerID, playerInventory, MonobankBlockEntity.this,
                    MonobankBlockEntity.this.getExtraInfo(player));
        }
    };
    private final MenuProvider UNLOCKING_MENU_PROVIDER = new MenuProvider() {
        @Override
        public Component getDisplayName() {
            return TextUtil.translate("gui.monobank.unlocking", MonobankBlockEntity.this.getName());
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerID, Inventory playerInventory, Player player) {
            return new UnlockingMenu(containerID, playerInventory, MonobankBlockEntity.this,
                    MonobankBlockEntity.this.lock.getCombination());
        }
    };
    private final ContainerOpenersCounter openersCounter = new MonobankOpenersCounter(this);
    private final DoorOpennessController doorOpennessController = new DoorOpennessController(0.5f,
        0.35f, 0.6f, 0.65f, 0.36f);

    // Serialized fields:
    private final MonobankItemStackHandler inventory;
    private final LazyOptional<IItemHandler> inventoryHandler;
    @Nullable
    protected ResourceLocation lootTable;
    protected long lootTableSeed;
    private final Lock lock;
    private Owner owner;
    private Component customName;
    public boolean breakInAttempted, breakInSucceeded;

    private float fullness = -1;

    public MonobankBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(Registry.BlockEntityTypes.MONOBANK.get(), pPos, pBlockState);
        this.inventory = new MonobankItemStackHandler(slot -> inventoryChanged(), 1);
        this.inventoryHandler = LazyOptional.of(() -> inventory);
        lock = new Lock(this.getBlockPos(), this::onLockedChanged, this::onLockInventoryChanged, this::getLevel);
        owner = Owner.none();
    }

    public static void appendHoverText(ItemStack stack, BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("BlockEntityTag", CompoundTag.TAG_COMPOUND)) {
            CompoundTag blockEntityTag = tag.getCompound("BlockEntityTag");
            if (blockEntityTag.contains(LOCK_TAG, CompoundTag.TAG_COMPOUND)) {
                CompoundTag lockTag = blockEntityTag.getCompound(LOCK_TAG);
                boolean locked = lockTag.getBoolean("Locked");
                if (locked)
                    tooltip.add(TextUtil.translate("tooltip.locked").withStyle(ChatFormatting.GRAY));

                if (blockEntityTag.contains(LOOT_TABLE_TAG, CompoundTag.TAG_STRING)) {
                    String lootTable = blockEntityTag.getString(LOOT_TABLE_TAG);
                    tooltip.add(TextUtil.translate("tooltip.loot_table", lootTable)
                            .withStyle(ChatFormatting.DARK_GRAY));
                }

                if (lockTag.contains("CombinationTable", CompoundTag.TAG_STRING)) {
                    tooltip.add(TextUtil.translate("tooltip.combination_table", lockTag.getString("CombinationTable"))
                            .withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
    }


    // Lock

    private boolean isUnlocking = false;
    private int unlockingCountdown = 0;
    private int unlockingCountdownMax = 0; // Used to calculate frequency of clicks when unlocking.

    public Lock getLock() {
        return lock;
    }

    public boolean replaceLock(Player player, Combination combination) {
        if (!Configuration.CAN_REPLACE_OTHER_PLAYERS_LOCKS.get() && getOwner().isPlayerOwned() && !getOwner().isOwnedBy(player)) {
            LogUtils.getLogger().error("Tried to replace lock in other player owned Monobank. " +
                    "(without enabling in config). Lock will not be replaced.");
            return false;
        }

        getLock().setCombination(combination);
        setOwner(player);
        playSoundAtDoor(Registry.Sounds.MONOBANK_CLICK.get()); // TODO: Lock Replacement Sound

        if (player instanceof ServerPlayer serverPlayer)
            Registry.Advancements.MONOBANK_LOCK_REPLACED.trigger(serverPlayer);

        return true;
    }

    public boolean isUnlocking() {
        return isUnlocking;
    }

    /**
     * Starts the countdown after which Monobank will unlock.
     */
    public void startUnlocking() {
        startUnlocking(getLevel().getRandom().nextInt(20, 61));
    }

    /**
     * Starts the countdown for specified amount of ticks after which Monobank will unlock.
     */
    public void startUnlocking(int ticks) {
        if (!isUnlocking()) {

            List<? extends Player> players = level.players();
            for (Player player : players) {
                if (player.containerMenu instanceof UnlockingMenu unlockingMenu && unlockingMenu.monobankEntity == this) {
                    checkAndPunishForCrime(player, Thief.Offence.HEAVY);
                    if (getOwner().isPlayerOwned() && !getOwner().isOwnedBy(player)) {
                        breakInSucceeded = true;
                        setChanged();
                    }

                    if (player instanceof ServerPlayer serverPlayer)
                        Registry.Advancements.MONOBANK_UNLOCKED.trigger(serverPlayer);
                }
            }

            isUnlocking = true;
            unlockingCountdown = ticks;
            unlockingCountdownMax = ticks;
        }
    }

    private void onLockedChanged() {
        boolean isLocked = lock.isLocked();
        doorOpennessController.setLocked(isLocked);
        unlockingCountdown = -1;
        isUnlocking = false;

        if (level != null && !level.isClientSide) { // Level is null when world loading, idk why.
            SoundEvent sound = isLocked ? Registry.Sounds.MONOBANK_LOCK.get() : Registry.Sounds.MONOBANK_UNLOCK.get();
            playSoundAtDoor(level, worldPosition, getBlockState(), sound, 1f);
        }

        setChanged();
    }

    private void onLockInventoryChanged(Integer slot) {
        if (level.isClientSide)
            return;

        Combination combination = getLock().getCombination();

        // Click when player places matching item in unlocking slot.
        if (combination.matches(slot, getLock().getInventory().getStackInSlot(slot).getItem()))
            playSoundAtDoor(Registry.Sounds.MONOBANK_CLICK.get());

        List<ItemStack> keys = new ArrayList<>();
        for (int i = 0; i < getLock().getInventory().getSlots(); i++) {
            keys.add(getLock().getInventory().getStackInSlot(i));
        }

        if (combination.matches(keys.stream().map(itemStack -> itemStack.getItem()).collect(Collectors.toList()))) {
            this.startUnlocking();
            dropItemsAtDoor(keys);
        }
    }

    public boolean checkAndPunishForCrime(Player player, Thief.Offence offence) {
        boolean crimeAgainstPlayer = Configuration.THIEF_OPENING_PLAYER_OWNED_IS_A_CRIME.get() && getOwner().isPlayerOwned() && !getOwner().isOwnedBy(player);
        boolean crimeAgainstNPC = getOwner().getType() == Owner.Type.NPC;
        if (crimeAgainstPlayer || crimeAgainstNPC) {
            List<LivingEntity> witnesses = Thief.getWitnesses(player);
            if (witnesses.size() > 0) {
                Thief.declareThief(player, witnesses, Thief.Offence.LIGHT);
                return true;
            }
        }
        return false;
    }


    // Save/Load

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!trySaveLootTable(tag))
            tag.put(INVENTORY_TAG, inventory.serializeNBT());
        tag.put(LOCK_TAG, lock.serializeNBT());
        if (owner.getType() != Owner.Type.NONE)
            tag.put(OWNER_TAG, owner.serializeNBT());
        if (this.customName != null)
            tag.putString(CUSTOM_NAME_TAG, Component.Serializer.toJson(this.customName));

        if (getOwner().isPlayerOwned()) {
            tag.putBoolean(BREAK_IN_SUCCEEDED_TAG, breakInSucceeded);
            tag.putBoolean(BREAK_IN_ATTEMPTED_TAG, breakInAttempted);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (!tryLoadLootTable(tag))
            inventory.deserializeNBT(tag.getCompound(INVENTORY_TAG));
        lock.deserializeNBT(tag.getCompound(LOCK_TAG));
        if (tag.contains(OWNER_TAG, CompoundTag.TAG_COMPOUND))
            owner.deserializeNBT(tag.getCompound(OWNER_TAG));
        if (tag.contains(CUSTOM_NAME_TAG, CompoundTag.TAG_STRING))
            this.customName = Component.Serializer.fromJson(tag.getString(CUSTOM_NAME_TAG));
        breakInSucceeded = tag.getBoolean(BREAK_IN_SUCCEEDED_TAG);
        breakInAttempted = tag.getBoolean(BREAK_IN_ATTEMPTED_TAG);
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

    public void unpackLootTable(@Nullable Player player, boolean includeCombination) {
        if (includeCombination)
            getLock().tryUnpackCombinationTable();

        if (this.lootTable != null && this.level != null && this.level.getServer() != null) {
            if (!this.inventory.getStackInSlot(0).isEmpty()) {
                LogUtils.getLogger().warn("Tried to unpack Loot Table while Monobank is not empty. Loot Table will not be unpacked.");
                return;
            }

            LootTable loottable = this.level.getServer().getLootTables().get(this.lootTable);
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)player, this.lootTable);
            }

            this.lootTable = null;
            LootContext.Builder lootContextBuilder = (new LootContext.Builder((ServerLevel)this.level))
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.worldPosition))
                    .withOptionalRandomSeed(this.lootTableSeed);
            if (player != null)
                lootContextBuilder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);

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
        if (!placer.level.isClientSide && placer instanceof Player player) {

            if (this.owner.getType() == Owner.Type.NONE)
                setOwner(player);

            if (!getLock().hasCombinationOrCombinationTable())
                getLock().setCombinationTable(Monobank.resource("combination/default"));

            setChanged();
        }
    }


    // GUI
    public void openUnlockingGui(ServerPlayer player) {
        if (lock.isLocked()) {

            checkAndPunishForCrime(player, Thief.Offence.LIGHT);

            if (getLock().getCombination().isEmpty())
                startUnlocking(); // Open straight up when no combination is set:
            else {
                if (getOwner().isPlayerOwned() && !getOwner().isOwnedBy(player)) {
                    breakInAttempted = true;
                    setChanged();
                }
                NetworkHooks.openScreen(player, this.UNLOCKING_MENU_PROVIDER, buffer -> {
                    buffer.writeBlockPos(worldPosition);
                    lock.getCombination().toBuffer(buffer);
                });
            }
        }
    }
    public void open(ServerPlayer player) {
        unpackLootTable(player, true);
        checkAndPunishForCrime(player, Thief.Offence.MODERATE);
        NetworkHooks.openScreen(player, this.OPEN_MENU_PROVIDER, buffer -> {
            buffer.writeBlockPos(worldPosition);
            getExtraInfo(player).toBuffer(buffer);
        });
    }


    // Inventory

    public IItemHandler getUnlockingInventoryHandler() {
        return lock.getInventory();
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


    /**
     * Used to provide gui with extra info.
     */
    public MonobankExtraInfo getExtraInfo(Player player) {
        return new MonobankExtraInfo(getOwner().isOwnedBy(player), breakInAttempted, breakInSucceeded);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (this.remove || getLock().isLocked())
            return super.getCapability(capability, side);

        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            unpackLootTable(null, false);
            return this.inventoryHandler.cast();
        }

        return super.getCapability(capability, side);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        inventoryHandler.invalidate();
    }

    public void inventoryChanged() {
        updateFullness();
        this.setChanged();
        // Advancement
        if (!level.isClientSide && getOwner().isPlayerOwned()) {
            @Nullable ServerPlayer player = level.getServer().getPlayerList().getPlayer(getOwner().getUuid());
            if (player != null)
                Registry.Advancements.MONOBANK_INVENTORY_CHANGED.trigger(player, getStoredItemStack());
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
                monobankEntity.unpackLootTable(null, false);

            if (monobankEntity.unlockingCountdown > 0) {
                // Calculating frequency of clicks (closer to unlocking -> more time between clicks):
                int max = (int)Math.ceil(Math.log(monobankEntity.unlockingCountdownMax)) + 1;
                int current = (int)Math.ceil(Math.log(monobankEntity.unlockingCountdown));
                int freq = max - current;
                if (monobankEntity.unlockingCountdown % freq == 0)
                    monobankEntity.playSoundAtDoor(monobankEntity.getLevel(), monobankEntity.getBlockPos(),
                            monobankEntity.getBlockState(), Registry.Sounds.MONOBANK_CLICK.get(), 0.5f);

                monobankEntity.unlockingCountdown--;
            }
            else if (monobankEntity.isUnlocking() && monobankEntity.unlockingCountdown <= 0)
                monobankEntity.getLock().setLocked(false);
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
            // Reset warnings:
            this.breakInAttempted = false;
            this.breakInSucceeded = false;
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
