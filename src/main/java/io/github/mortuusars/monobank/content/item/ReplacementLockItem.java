package io.github.mortuusars.monobank.content.item;

import io.github.mortuusars.monobank.content.monobank.MonobankBlockEntity;
import io.github.mortuusars.monobank.content.monobank.lock_replacement.LockReplacementMenu;
import io.github.mortuusars.monobank.util.TextUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ReplacementLockItem extends Item {
    public ReplacementLockItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) { }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos clickedPos = context.getClickedPos();

        BlockEntity blockEntityAtPos = level.getBlockEntity(clickedPos);

        if (!(blockEntityAtPos instanceof MonobankBlockEntity monobankEntity))
            return InteractionResult.FAIL;

        if (monobankEntity.getOwner().isPlayerOwned() && !monobankEntity.getOwner().isOwnedBy(player)) { //TODO: config
            player.displayClientMessage(TextUtil.translate("interaction.message.cannot_change_lock_not_owner"), true);
            return InteractionResult.FAIL;
        }

        if (monobankEntity.getLock().isLocked()) {
            player.displayClientMessage(TextUtil.translate("interaction.message.cannot_change_lock_is_locked"), true);
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide) {
            NetworkHooks.openGui(((ServerPlayer) player), new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return TextUtil.translate("gui.monobank.lock_replacement", monobankEntity.getName());
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                    return new LockReplacementMenu(containerId, playerInventory, monobankEntity);
                }
            }, clickedPos);

        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
