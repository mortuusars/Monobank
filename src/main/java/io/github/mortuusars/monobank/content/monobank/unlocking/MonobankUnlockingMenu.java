package io.github.mortuusars.monobank.content.monobank.unlocking;

import io.github.mortuusars.monobank.Registry;
import io.github.mortuusars.monobank.content.monobank.MonobankBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;

public class MonobankUnlockingMenu extends AbstractContainerMenu {

    public final MonobankBlockEntity blockEntity;
    private final ContainerLevelAccess canInteractWithCallable;
    protected final Level level;

    public MonobankUnlockingMenu(final int containerID, final Inventory playerInventory, final MonobankBlockEntity blockEntity) {
        super(Registry.MenuTypes.MONOBANK.get(), containerID);
        this.blockEntity = blockEntity;
        this.level = playerInventory.player.level;
        this.canInteractWithCallable = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

//        // Monobank slot
//        blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(itemHandler -> {
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

    public static MonobankUnlockingMenu fromBuffer(int containerID, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new MonobankUnlockingMenu(containerID, playerInventory, getBlockEntity(playerInventory, buffer));
    }

    // Called server-side.
    @Override
    public boolean stillValid(Player player) {
        return blockEntity.isLocked() && stillValid(canInteractWithCallable, player, Registry.Blocks.MONOBANK.get());
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
