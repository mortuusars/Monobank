package io.github.mortuusars.monobank.content.item;

import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.PlatformHelper;
import io.github.mortuusars.monobank.world.block.monobank.MonobankBlockEntity;
import io.github.mortuusars.monobank.content.monobank.lock_replacement.LockReplacementMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class ReplacementLockItem extends Item {
    public ReplacementLockItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();

        BlockEntity blockEntityAtPos = level.getBlockEntity(clickedPos);

        if (!(blockEntityAtPos instanceof MonobankBlockEntity blockEntity)) {
            return InteractionResult.PASS;
        }

        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.sidedSuccess(true);
        }

        if (blockEntity.getLock().isLocked()) {
            player.displayClientMessage(Component.translatable(
                    "monobank.message.replacement_lock.cannot_replace_when_locked"), true);
            blockEntity.playSoundAtDoor(Monobank.SoundEvents.MONOBANK_CLICK.get());
            return InteractionResult.FAIL;
        }

        if (blockEntity.canReplaceLock(player)) {
            player.displayClientMessage(Component.translatable(
                    "monobank.message.replacement_lock.cannot_replace_not_owner"), true);
            blockEntity.playSoundAtDoor(Monobank.SoundEvents.MONOBANK_CLICK.get());
            return InteractionResult.FAIL;
        }

        PlatformHelper.openMenu(player, new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return Component.translatable("monobank.gui.monobank.lock_replacement", blockEntity.getName());
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                return new LockReplacementMenu(containerId, playerInventory, blockEntity);
            }
        }, buffer -> buffer.writeBlockPos(clickedPos));

        return InteractionResult.sidedSuccess(false);
    }
}
