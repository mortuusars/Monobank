package io.github.mortuusars.monobank.content.monobank;

import io.github.mortuusars.monobank.registry.ModBlocks;
import io.github.mortuusars.monobank.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;

public class MonobankMenu extends AbstractContainerMenu {

    public final MonobankBlockEntity blockEntity;
    private final ContainerLevelAccess canInteractWithCallable;
    protected final Level level;

    public MonobankMenu(final int containerID, final Inventory playerInventory, final MonobankBlockEntity blockEntity, ContainerData containerData) {
        super(ModMenuTypes.MONOBANK.get(), containerID);
        this.blockEntity = blockEntity;
        this.level = playerInventory.player.level;
        this.canInteractWithCallable = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    public MonobankMenu(final int containerID, final Inventory playerInventory, final FriendlyByteBuf data) {
        this(containerID, playerInventory, getBlockEntity(playerInventory, data), new SimpleContainerData(1));
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

    @Override
    public boolean stillValid(Player player) {
        return stillValid(canInteractWithCallable, player, ModBlocks.MONOBANK.get());
    }
}
