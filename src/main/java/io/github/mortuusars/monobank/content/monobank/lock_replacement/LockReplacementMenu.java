package io.github.mortuusars.monobank.content.monobank.lock_replacement;

import io.github.mortuusars.monobank.Registry;
import io.github.mortuusars.monobank.content.monobank.MonobankBlockEntity;
import io.github.mortuusars.monobank.content.monobank.unlocking.Combination;
import io.github.mortuusars.monobank.core.inventory.CombinationContainer;
import io.github.mortuusars.monobank.core.inventory.GhostSlot;
import io.github.mortuusars.monobank.util.TextUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LockReplacementMenu extends AbstractContainerMenu {
    private final MonobankBlockEntity monobankEntity;
    private final Level level;
    private final ContainerLevelAccess canInteractWithCallable;
    private final CombinationContainer combinationContainer;
    public LockReplacementMenu(final int containerID, final Inventory playerInventory,
                               final MonobankBlockEntity monobankEntity) {
        super(Registry.MenuTypes.MONOBANK_LOCK_REPLACEMENT.get(), containerID);
        this.monobankEntity = monobankEntity;
        this.level = playerInventory.player.level;
        this.canInteractWithCallable = ContainerLevelAccess.create(monobankEntity.getLevel(), monobankEntity.getBlockPos());

        this.combinationContainer = new CombinationContainer();

        this.addSlot(new GhostSlot(combinationContainer, 0, 59, 35));
        this.addSlot(new GhostSlot(combinationContainer, 1, 81, 35));
        this.addSlot(new GhostSlot(combinationContainer, 2, 101, 35));

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
    }

    public static LockReplacementMenu fromBuffer(int containerID, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new LockReplacementMenu(containerID, playerInventory, getBlockEntity(playerInventory, buffer));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        if (index < Combination.SIZE) { // Remove key
            this.slots.get(index).set(ItemStack.EMPTY);
        }
        else if (index < this.slots.size()) {
            Slot clickedSlot = this.slots.get(index);
            if (clickedSlot.hasItem()) {
                ItemStack clickedItemStack = clickedSlot.getItem();
                for (int i = 0; i < Combination.SIZE; i++) {
                    Slot combinationSlot = this.slots.get(i);
                    if (combinationSlot.getItem().isEmpty()) {
                        combinationSlot.set(new ItemStack(clickedItemStack.getItem()));
                        break;
                    }
                }
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= Combination.SIZE || slotId < 0) {
            super.clicked(slotId, button, clickType, player);
            return;
        }

        Slot combinationSlot = this.slots.get(slotId);

        if (!getCarried().isEmpty())
            combinationSlot.set(new ItemStack(getCarried().getItem()));
        else
            combinationSlot.set(ItemStack.EMPTY);
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonID) {

        List<ItemStack> combination = new ArrayList<>(Combination.SIZE);

        for (int i = 0; i < Combination.SIZE; i++) {
            combination.add(this.slots.get(i).getItem());
        }

        monobankEntity.getLock().setCombination(combination.get(0).getItem(), combination.get(1).getItem(), combination.get(2).getItem());
        monobankEntity.setOwner(player);
        player.displayClientMessage(TextUtil.translate("message.lock_replaced"), true);
        monobankEntity.playSoundAtDoor(Registry.Sounds.MONOBANK_CLICK.get()); // TODO: Lock Replacement Sound

        // Consume item:
        ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemInHand.is(Registry.Items.REPLACEMENT_LOCK.get()))
            itemInHand.shrink(1);
        else {
            itemInHand = player.getItemInHand(InteractionHand.OFF_HAND);
            if (itemInHand.is(Registry.Items.REPLACEMENT_LOCK.get()))
                itemInHand.shrink(1);
        }

        player.closeContainer();
        return true;
    }

    // Called server-side.
    @Override
    public boolean stillValid(Player player) {
        return (!monobankEntity.getOwner().isPlayerOwned() || monobankEntity.getOwner().isOwnedBy(player)) &&
                (player.getItemInHand(InteractionHand.MAIN_HAND).is(Registry.Items.REPLACEMENT_LOCK.get()) ||
                player.getItemInHand(InteractionHand.OFF_HAND).is(Registry.Items.REPLACEMENT_LOCK.get())) &&
                stillValid(canInteractWithCallable, player, Registry.Blocks.MONOBANK.get());
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
