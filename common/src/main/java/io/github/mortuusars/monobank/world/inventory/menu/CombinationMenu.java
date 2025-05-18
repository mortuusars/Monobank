package io.github.mortuusars.monobank.world.inventory.menu;

import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.world.block.monobank.MonobankBlockEntity;
import io.github.mortuusars.monobank.world.block.monobank.component.Combination;
import io.github.mortuusars.monobank.world.inventory.MatchTemplateSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class CombinationMenu extends AbstractContainerMenu implements ContainerListener {
    protected final Container keyContainer = new SimpleContainer(Combination.SIZE);
    protected final MonobankBlockEntity blockEntity;
    protected final Combination combination;
    protected final Player player;
    protected final Level level;

    public CombinationMenu(int containerID, Inventory playerInventory, MonobankBlockEntity blockEntity, Combination combination) {
        super(Monobank.MenuTypes.MONOBANK_COMBINATION.get(), containerID);
        this.blockEntity = blockEntity;
        this.combination = combination;
        this.player = playerInventory.player;
        this.level = playerInventory.player.level();

        addSlotListener(this);

        addSlot(new MatchTemplateSlot(keyContainer, 0, 80, 35, new ItemStack(combination.getItem(0))));
        addSlot(new MatchTemplateSlot(keyContainer, 1, 101, 35, new ItemStack(combination.getItem(1))));
        addSlot(new MatchTemplateSlot(keyContainer, 2, 122, 35, new ItemStack(combination.getItem(2))));

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
        if (!level.isClientSide && blockEntity.getLock().getCombination().isEmpty()) {
            blockEntity.startUnlocking(playerInventory.player);
        }
    }

    public static CombinationMenu fromBuffer(int containerID, Inventory playerInventory, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        BlockEntity blockEntityAtPos = playerInventory.player.level().getBlockEntity(pos);
        if (blockEntityAtPos instanceof MonobankBlockEntity blockEntity) {
            return new CombinationMenu(containerID, playerInventory, blockEntity, Combination.fromBuffer(buffer));
        } else {
            throw new IllegalStateException("Block entity at pos '" + pos + "' is not MonobankBlockEntity, but " + blockEntityAtPos);
        }
    }

    // --

    public MonobankBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public Combination getCombination() {
        return combination;
    }

    // --

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
            boolean insertedToMatching = false;
            for (int i = 0; i < Combination.SIZE; i++) {
                if (slots.get(i) instanceof MatchTemplateSlot matchTemplateSlot
                        && !matchTemplateSlot.hasItem()
                        && getCombination().matches(i, clickedSlotStack)
                        && matchTemplateSlot.mayPlace(clickedSlotStack)) {
                    matchTemplateSlot.safeInsert(clickedSlotStack.split(1));
                    insertedToMatching = true;
                    break;
                }
            }

            if (!insertedToMatching) {
                for (int i = 0; i < Combination.SIZE; i++) {
                    if (slots.get(i) instanceof MatchTemplateSlot matchTemplateSlot
                            && !matchTemplateSlot.hasItem()
                            && matchTemplateSlot.mayPlace(clickedSlotStack)) {
                        matchTemplateSlot.safeInsert(clickedSlotStack.split(1));
                        break;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void removed(Player player) {
        if (!player.level().isClientSide) {
            for (int i = 0; i < keyContainer.getContainerSize(); i++) {
                ItemStack item = keyContainer.getItem(i);
                if (!item.isEmpty()) {
                    if (!player.addItem(item)) {
                        player.drop(item, false);
                    }
                    keyContainer.setItem(i, ItemStack.EMPTY); // Just to be sure.
                }
            }
        }

        super.removed(player);
    }

    // Called server-side.
    @Override
    public boolean stillValid(Player player) {
        return blockEntity.getLock().isLocked() && !blockEntity.getLock().isUnlocking() && blockEntity.stillValid(player);
    }

    @Override
    public void slotChanged(AbstractContainerMenu containerToSend, int slotIndex, ItemStack stack) {
        if (slotIndex >= 0 && slotIndex < Combination.SIZE && getCombination().matches(slotIndex, stack)) {
            blockEntity.playSoundAtDoor(Monobank.SoundEvents.MONOBANK_CLICK.get());
        }

        if (getCombination().matches(keyContainer)) {
            blockEntity.startUnlocking(player);
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, int value) { }
}
