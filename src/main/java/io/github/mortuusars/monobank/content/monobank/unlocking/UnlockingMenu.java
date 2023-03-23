package io.github.mortuusars.monobank.content.monobank.unlocking;

import io.github.mortuusars.monobank.Registry;
import io.github.mortuusars.monobank.content.monobank.MonobankBlockEntity;
import io.github.mortuusars.monobank.core.inventory.UnlockingSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;

import java.util.Objects;

public class UnlockingMenu extends AbstractContainerMenu {

    public final MonobankBlockEntity monobankEntity;
    public final Combination combination;
    private final ContainerLevelAccess canInteractWithCallable;
    protected final Level level;

    public UnlockingMenu(final int containerID, final Inventory playerInventory,
                         final MonobankBlockEntity monobankEntity, final Combination combination) {
        super(Registry.MenuTypes.MONOBANK_UNLOCKING.get(), containerID);
        this.monobankEntity = monobankEntity;
        this.combination = combination;
        this.level = playerInventory.player.level;
        this.canInteractWithCallable = ContainerLevelAccess.create(monobankEntity.getLevel(), monobankEntity.getBlockPos());

        IItemHandler unlockingInventoryHandler = monobankEntity.getUnlockingInventoryHandler();
        this.addSlot(new UnlockingSlot(unlockingInventoryHandler, 0, 80, 35, new ItemStack(combination.getItemInSlot(0))));
        this.addSlot(new UnlockingSlot(unlockingInventoryHandler, 1, 101, 35, new ItemStack(combination.getItemInSlot(1))));
        this.addSlot(new UnlockingSlot(unlockingInventoryHandler, 2, 122, 35, new ItemStack(combination.getItemInSlot(2))));

        // Player hotbar slots
        for(int column = 0; column < 9; ++column) {
            this.addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }

        // Player inventory slots
        for(int row = 0; row < 3; ++row) {
            for(int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        // Empty combination should be handled earlier - this is to be safe.
        if (!level.isClientSide && monobankEntity.getLock().getCombination().isEmpty())
            monobankEntity.startUnlocking();
    }

    public static UnlockingMenu fromBuffer(int containerID, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new UnlockingMenu(containerID, playerInventory,
                getBlockEntity(playerInventory, buffer), Combination.fromBuffer(buffer));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        Slot clickedSlot = this.slots.get(index);
        if (clickedSlot == null || !clickedSlot.hasItem())
            return ItemStack.EMPTY;

        ItemStack clickedSlotStack = clickedSlot.getItem();

        if (index < 3) { // From lock
            // Count should be 1 always, but if not - remove all:
            ItemStack removedStack = clickedSlot.remove(clickedSlotStack.getCount());

            // Try to insert removed portion into player's inventory.
            // Stack's count will be decreased by the amount inserted.
            this.moveItemStackTo(removedStack, 3, this.slots.size(), false);

            if (!removedStack.isEmpty())
                // Insert remainder back:
                clickedSlot.safeInsert(removedStack);
        }
        else {
            for (int i = 0; i < Combination.SIZE; i++) {
                if (slots.get(i) instanceof UnlockingSlot unlockingSlot
                        && !unlockingSlot.hasItem()
                        && unlockingSlot.getKeyway().sameItem(clickedSlotStack)
                        && unlockingSlot.mayPlace(clickedSlotStack)) {
                    unlockingSlot.safeInsert(clickedSlotStack.split(1));
                    break;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void removed(Player player) {
        if (!player.level.isClientSide) {
            for (int i = 0; i < Combination.SIZE; i++) {
                IItemHandler unlockingInventory = monobankEntity.getUnlockingInventoryHandler();
                if (!unlockingInventory.getStackInSlot(i).isEmpty()) {
                    ItemStack itemStack = unlockingInventory.extractItem(i, 1, false);
                    player.drop(itemStack, false);
                }
            }
        }

        super.removed(player);
    }

    // Called server-side.
    @Override
    public boolean stillValid(Player player) {
        return monobankEntity.getLock().isLocked() && !monobankEntity.isUnlocking()
                && stillValid(canInteractWithCallable, player, Registry.Blocks.MONOBANK.get());
    }

    private static MonobankBlockEntity getBlockEntity(final Inventory playerInventory, final FriendlyByteBuf data) {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null");
        Objects.requireNonNull(data, "data cannot be null");
        final BlockEntity blockEntityAtPos = playerInventory.player.level.getBlockEntity(data.readBlockPos());
        if (blockEntityAtPos instanceof MonobankBlockEntity monobankBlockEntity) {
            return monobankBlockEntity;
        }
        throw new IllegalStateException("Block entity is not correct! " + blockEntityAtPos);
    }
}
