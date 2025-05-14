package io.github.mortuusars.monobank.content.monobank.unlocking;

import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.world.block.monobank.MonobankBlockEntity;
import io.github.mortuusars.monobank.world.block.monobank.component.Combination;
import io.github.mortuusars.monobank.world.inventory.MatchTemplateSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CombinationMenu extends AbstractContainerMenu {
    protected final MonobankBlockEntity monobankEntity;
    protected final Combination combination;
    protected final Level level;
    protected final Container container = new SimpleContainer(Combination.SIZE);

    public CombinationMenu(final int containerID, final Inventory playerInventory,
                           final MonobankBlockEntity monobankEntity, final Combination combination) {
        super(Monobank.MenuTypes.MONOBANK_COMBINATION.get(), containerID);
        this.monobankEntity = monobankEntity;
        this.combination = combination;
        this.level = playerInventory.player.level();

        addSlot(new MatchTemplateSlot(container, 0, 80, 35, new ItemStack(combination.getItemInSlot(0))));
        addSlot(new MatchTemplateSlot(container, 1, 101, 35, new ItemStack(combination.getItemInSlot(1))));
        addSlot(new MatchTemplateSlot(container, 2, 122, 35, new ItemStack(combination.getItemInSlot(2))));

        // Player hotbar slots
        for (int column = 0; column < 9; ++column) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }

        // Player inventory slots
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        // Empty combination should be handled earlier - this is just to be safe.
        if (!level.isClientSide && monobankEntity.getLock().getCombination().isEmpty()) {
            monobankEntity.startUnlocking(playerInventory.player);
        }
    }

    public static CombinationMenu fromBuffer(int containerID, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new CombinationMenu(containerID, playerInventory,
                getBlockEntity(playerInventory, buffer), Combination.fromBuffer(buffer));
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        Slot clickedSlot = this.slots.get(index);
        if (!clickedSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack clickedSlotStack = clickedSlot.getItem();

        if (index < 3) { // From lock
            // Count should be 1 always, but if not - remove all:
            ItemStack removedStack = clickedSlot.remove(clickedSlotStack.getCount());

            // Try to insert removed portion into player's inventory.
            // Stack's count will be decreased by the amount inserted.
            this.moveItemStackTo(removedStack, 3, this.slots.size(), false);

            if (!removedStack.isEmpty()) {
                // Insert remainder back:
                clickedSlot.safeInsert(removedStack);
            }
        } else {
            for (int i = 0; i < Combination.SIZE; i++) {
                if (slots.get(i) instanceof MatchTemplateSlot matchTemplateSlot
                        && !matchTemplateSlot.hasItem()
                        && ItemStack.isSameItem(matchTemplateSlot.getTemplate(), clickedSlotStack)
                        && matchTemplateSlot.mayPlace(clickedSlotStack)) {
                    matchTemplateSlot.safeInsert(clickedSlotStack.split(1));
                    break;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void removed(Player player) {
        if (!player.level().isClientSide) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack item = container.getItem(i);
                if (!item.isEmpty()) {
                    if (!player.addItem(item)) {
                        player.drop(item, false);
                    }
                    container.setItem(i, ItemStack.EMPTY); // Just to be sure.
                }
            }
        }

        super.removed(player);
    }

    // Called server-side.
    @Override
    public boolean stillValid(Player player) {
        return monobankEntity.getLock().isLocked() && !monobankEntity.isUnlocking() && monobankEntity.stillValid(player);
    }

    private static MonobankBlockEntity getBlockEntity(final Inventory playerInventory, final FriendlyByteBuf data) {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null");
        Objects.requireNonNull(data, "data cannot be null");
        final BlockEntity blockEntityAtPos = playerInventory.player.level().getBlockEntity(data.readBlockPos());
        if (blockEntityAtPos instanceof MonobankBlockEntity monobankBlockEntity) {
            return monobankBlockEntity;
        }
        throw new IllegalStateException("Block entity is not correct! " + blockEntityAtPos);
    }
}
