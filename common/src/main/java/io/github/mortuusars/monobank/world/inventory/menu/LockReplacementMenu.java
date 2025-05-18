package io.github.mortuusars.monobank.world.inventory.menu;

import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.world.block.monobank.MonobankBlockEntity;
import io.github.mortuusars.monobank.world.block.monobank.component.Combination;
import io.github.mortuusars.monobank.world.inventory.CombinationContainer;
import io.github.mortuusars.monobank.world.inventory.GhostSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class LockReplacementMenu extends AbstractContainerMenu {
    public static final int CONFIRM_BUTTON_ID = 0;

    protected final MonobankBlockEntity monobankEntity;
    protected final CombinationContainer combinationContainer;

    public LockReplacementMenu(final int containerID, final Inventory playerInventory,
                               final MonobankBlockEntity monobankEntity) {
        super(Monobank.MenuTypes.MONOBANK_LOCK_REPLACEMENT.get(), containerID);
        this.monobankEntity = monobankEntity;

        this.combinationContainer = new CombinationContainer();

        this.addSlot(new GhostSlot(combinationContainer, 0, 59, 35));
        this.addSlot(new GhostSlot(combinationContainer, 1, 80, 35));
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
        MonobankBlockEntity result;
        final BlockEntity blockEntityAtPos = playerInventory.player.level().getBlockEntity(buffer.readBlockPos());
        if (blockEntityAtPos instanceof MonobankBlockEntity monobankBlockEntity) {
            result = monobankBlockEntity;
        } else {
            throw new IllegalStateException("Block entity is not correct! " + blockEntityAtPos);
        }
        return new LockReplacementMenu(containerID, playerInventory, result);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        if (index < 3) { // Remove key
            this.slots.get(index).set(ItemStack.EMPTY);
        }
        else if (index < this.slots.size()) {
            Slot clickedSlot = this.slots.get(index);
            if (clickedSlot.hasItem()) {
                ItemStack clickedItemStack = clickedSlot.getItem();
                for (int i = 0; i < 3; i++) {
                    Slot combinationSlot = this.slots.get(i);
                    if (combinationSlot.getItem().isEmpty() && combinationSlot.mayPlace(clickedItemStack)) {
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

        if (!getCarried().isEmpty()) {
            combinationSlot.set(new ItemStack(getCarried().getItem()));
        } else {
            combinationSlot.set(ItemStack.EMPTY);
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonID) {
        if (buttonID != CONFIRM_BUTTON_ID) return false;

        if (slots.stream().limit(3).anyMatch(s -> s.getItem().is(Monobank.Tags.Items.LOCK_BLACKLIST))) {
            return true;
        }

        boolean replaced = monobankEntity.replaceLock(player,
                new Combination(slots.get(0).getItem(), slots.get(1).getItem(), slots.get(2).getItem()));
        if (replaced) {
            player.displayClientMessage(Component.translatable("monobank.message.lock_replaced"), true);

            // Consume item:
            ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (itemInHand.is(Monobank.Items.REPLACEMENT_LOCK.get()))
                itemInHand.shrink(1);
            else {
                itemInHand = player.getItemInHand(InteractionHand.OFF_HAND);
                if (itemInHand.is(Monobank.Items.REPLACEMENT_LOCK.get()))
                    itemInHand.shrink(1);
            }

            player.closeContainer();
            return true;
        }

        return false;
    }

    // Called server-side.
    @Override
    public boolean stillValid(Player player) {
        return (!monobankEntity.getOwner().isPlayerOwned() || monobankEntity.getOwner().isOwnedBy(player)) &&
                (player.getItemInHand(InteractionHand.MAIN_HAND).is(Monobank.Items.REPLACEMENT_LOCK.get()) ||
                        player.getItemInHand(InteractionHand.OFF_HAND).is(Monobank.Items.REPLACEMENT_LOCK.get())) &&
                monobankEntity.stillValid(player);
    }
}
