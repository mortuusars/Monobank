package io.github.mortuusars.monobank.content.monobank;

import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.world.block.monobank.component.MonobankExtraInfo;
import io.github.mortuusars.monobank.world.block.monobank.MonobankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class MonobankMenu extends AbstractContainerMenu {
    public static final int MONOBANK_SLOT_INDEX = 0;
    public static final int MONOBANK_SLOT_X = 71;
    public static final int MONOBANK_SLOT_Y = 26;
    public static final int MONOBANK_SLOT_SIZE = 34;

    public static final int TRANSFER_SINGLE_BUTTON_ID = -101;
    public static final int TRANSFER_ALL_BUTTON_ID = -102;

    public final MonobankBlockEntity blockEntity;
    public MonobankExtraInfo extraInfo;
    protected final Level level;

    public MonobankMenu(final int containerID, final Inventory playerInventory, final MonobankBlockEntity blockEntity, final MonobankExtraInfo extraInfo) {
        super(Monobank.MenuTypes.MONOBANK.get(), containerID);
        this.blockEntity = blockEntity;
        this.level = playerInventory.player.level();
        this.extraInfo = extraInfo;

        blockEntity.startOpen(playerInventory.player);

        // Monobank slot
//        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(itemHandler -> {
//            this.addSlot(new BigItemHandlerSlot(((MonobankItemStackHandler) itemHandler),
//                    0, MONOBANK_SLOT_X, MONOBANK_SLOT_Y, MONOBANK_SLOT_SIZE, MONOBANK_SLOT_SIZE));
//        });

        // Player hotbar slots
        for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }

        // Player inventory slots
        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
    }

    public static MonobankMenu fromBuffer(int containerID, Inventory playerInventory, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        BlockEntity blockEntityAtPos = playerInventory.player.level().getBlockEntity(pos);
        if (blockEntityAtPos instanceof MonobankBlockEntity blockEntity) {
            return new MonobankMenu(containerID, playerInventory, blockEntity, MonobankExtraInfo.fromBuffer(buffer));
        } else {
            throw new IllegalStateException("Block entity at pos '" + pos + "' is not MonobankBlockEntity, but " + blockEntityAtPos);
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        /*
            When opening other UI over ours (such as JEI),
            'removed' is called only client-side (server-side it's still open), when it probably shouldn't.
            This causes door to close while still in the menu, and not update its openness properly.
            So we are closing only server-side - which is then synchronized to client in OpenersCounter via block update.
         */
        if (!player.level().isClientSide)
            this.blockEntity.stopOpen(player);
    }

    public MonobankBlockEntity getBlockEntity() {
        return blockEntity;
    }

    /**
     * Handles QuickMove with modifiers from bank slot.
     * Uses (hopefully) unused button ids to differentiate between actions.
     */
    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {

        if (clickType != ClickType.QUICK_MOVE) {
            super.clicked(slotId, button, clickType, player);
            return;
        }

//        @Nullable IItemHandler itemHandler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
//        if (itemHandler == null)
//            return;

//        if (button == TRANSFER_SINGLE_BUTTON_ID) {
//            ItemStack extractedItem = itemHandler.extractItem(0, 1, false);
//            if (!player.addItem(extractedItem))
//                itemHandler.insertItem(0, extractedItem, false); // Insert remainder back into bank
//        }
//        else if (button == TRANSFER_ALL_BUTTON_ID) {
//            while (true) {
//                ItemStack extractedStack = itemHandler.extractItem(0, blockEntity.getStoredItemStack().getMaxStackSize(), false);
//
//                if (extractedStack.isEmpty())
//                    break;
//
//                player.addItem(extractedStack);
//
//                if (!extractedStack.isEmpty()) {
//                    itemHandler.insertItem(0, extractedStack, false); // Insert remainder back into bank
//                    break;
//                }
//            }
//        }
//        else {
            super.clicked(slotId, button, clickType, player);
//        }
    }

    /**
     * If ItemStack.EMPTY is returned - repeated insertions will be stopped.
     */
    @Override
    public @NotNull ItemStack quickMoveStack(Player pPlayer, int index) {
        Slot clickedSlot = this.slots.get(index);
        if (!clickedSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack clickedSlotItemStack = clickedSlot.getItem();

        if (index == MONOBANK_SLOT_INDEX) { // From Monobank
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
        else { // To Monobank:
            Slot monobankSlot = this.slots.get(MONOBANK_SLOT_INDEX);
//            if (!monobankSlot.hasItem() || ItemHandlerHelper.canItemStacksStack(monobankSlot.getItem(), clickedSlotItemStack)) {
//                ItemStack remainder = monobankSlot.safeInsert(clickedSlotItemStack);
//
//                // Insert remainder back into the inventory:
//                if (!remainder.isEmpty())
//                    clickedSlot.safeInsert(remainder);
//            }
        }

        return ItemStack.EMPTY;
    }

    // Called server-side.
    @Override
    public boolean stillValid(Player player) {
        return !blockEntity.getLock().isLocked() && blockEntity.stillValid(player);
    }

}
