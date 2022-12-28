package io.github.mortuusars.monobank.content.monobank;

import io.github.mortuusars.monobank.content.monobank.inventory.BigSlotItemHandler;
import io.github.mortuusars.monobank.content.monobank.inventory.MonobankItemStackHandler;
import io.github.mortuusars.monobank.registry.ModBlocks;
import io.github.mortuusars.monobank.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Objects;

public class MonobankMenu extends AbstractContainerMenu {

    public static final int MONOBANK_SLOT_INDEX = 0;

    public final MonobankBlockEntity blockEntity;
    private final ContainerLevelAccess canInteractWithCallable;
    protected final Level level;

    public MonobankMenu(final int containerID, final Inventory playerInventory, final MonobankBlockEntity blockEntity) {
        super(ModMenuTypes.MONOBANK.get(), containerID);
        this.blockEntity = blockEntity;
        this.level = playerInventory.player.level;
        this.canInteractWithCallable = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());


        // Monobank slot
        blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(itemHandler -> {
            this.addSlot(new BigSlotItemHandler(((MonobankItemStackHandler) itemHandler), 0, 80, 25));
        });

        // Player hotbar slots
        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // Player inventory slots
        for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    public static MonobankMenu fromBuffer(int containerID, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new MonobankMenu(containerID, playerInventory, getBlockEntity(playerInventory, buffer));
    }

    // This is called both clientside and serverside
    @Override
    public boolean clickMenuButton(Player player, int buttonActionID) {
        ScreenKeyModifier screenKeyModifier = ScreenKeyModifier.fromID(buttonActionID);
        WithdrawAction withdrawAction = WithdrawAction.fromKeyModifier(screenKeyModifier);

        ItemStack withdrawnItemStack;

        if (withdrawAction == WithdrawAction.STACK) {
            withdrawnItemStack = blockEntity.withdrawStack();
            player.addItem(withdrawnItemStack);
        }
        else if (withdrawAction == WithdrawAction.ALL) {
            do {
                withdrawnItemStack = blockEntity.withdrawStack();
            }
            while (player.addItem(withdrawnItemStack) && !withdrawnItemStack.isEmpty());
        }
        else {
            withdrawnItemStack = blockEntity.withdraw(1);
            player.addItem(withdrawnItemStack);
        }

//        player.drop(withdrawnItemStack, false);

        return true;
    }

    /**
     * If ItemStack.EMPTY is returned - repeated insertions will be stopped.
     */
    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {

        Slot clickedSlot = this.slots.get(pIndex);
        if (clickedSlot == null || !clickedSlot.hasItem())
            return ItemStack.EMPTY;

        ItemStack clickedSlotItemStack = clickedSlot.getItem();

        // From Monobank
        if (pIndex == MONOBANK_SLOT_INDEX) {

            // Remove proper portion of a stack (stack size or amount available - whatever is smaller)
            // getMaxStackSize is called on the ItemStack - and it will return vanilla stack size instead of the bank size.
            ItemStack removedStack = clickedSlot.remove(clickedSlotItemStack.getMaxStackSize());

            // Try to insert removed portion into player's inventory.
            // Stack's count will be decreased by the amount inserted.
            this.moveItemStackTo(removedStack, 1, this.slots.size(), false);

            // Insert remainder back into the bank:
            if (!removedStack.isEmpty())
                clickedSlot.safeInsert(removedStack);
        }
        else {
            // To Monobank:
            Slot monobankSlot = this.slots.get(MONOBANK_SLOT_INDEX);

            if (!monobankSlot.hasItem() || ItemHandlerHelper.canItemStacksStack(monobankSlot.getItem(), clickedSlotItemStack)) {
                ItemStack remainder = monobankSlot.safeInsert(clickedSlotItemStack);

                // Insert remainder back into the inventory:
                if (!remainder.isEmpty())
                    clickedSlot.safeInsert(remainder);
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(canInteractWithCallable, player, ModBlocks.MONOBANK.get());
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
