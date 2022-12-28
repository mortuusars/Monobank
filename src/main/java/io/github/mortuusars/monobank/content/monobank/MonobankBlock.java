package io.github.mortuusars.monobank.content.monobank;

import io.github.mortuusars.monobank.registry.ModBlockEntityTypes;
import io.github.mortuusars.monobank.util.TextUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"NullableProblems", "deprecation", "unused"})
public class MonobankBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public MonobankBlock() {
        super(Properties.of(Material.METAL, MaterialColor.COLOR_BLACK)
                .strength(20F, 1200F)
                .sound(SoundType.NETHERITE_BLOCK));

        registerDefaultState(this.getStateDefinition().any()
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return ModBlockEntityTypes.MONOBANK.get().create(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @javax.annotation.Nullable LivingEntity placer, ItemStack stack) {
        if (!(level.getBlockEntity(pos) instanceof MonobankBlockEntity monobankBlockEntity))
            return;

        if (stack.hasCustomHoverName())
            monobankBlockEntity.setCustomName(stack.getHoverName());

        // Owner from ItemStack is set from BlockEntityTag, in BlockEntity#load
        if (!monobankBlockEntity.hasOwner() && placer instanceof Player player)
            monobankBlockEntity.setOwner(player);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
        return entityType == ModBlockEntityTypes.MONOBANK.get() && level.isClientSide ?
               MonobankBlockEntity::doorAnimateTick : null;
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof MonobankBlockEntity monobankBlockEntity))
            return InteractionResult.FAIL;

        /*  ___________________________________________________
           | State:     |     Owner     |        Others        |
           |------------|---------------|----------------------|
           | Locked:    |     Unlock    |   Unlock With Code   |
           | Unlocked:  |   Lock, Open  |         Open         |
           | Public:    |      Open     |         Open         |
           |____________|_______________|______________________| */

        return player.isSecondaryUseActive() ?
                tryLockOrUnlock(blockState, player, level, pos, monobankBlockEntity) :
                tryOpen(blockState, player, level, pos, monobankBlockEntity);
    }

    private InteractionResult tryOpen(BlockState blockState, Player player, Level level, BlockPos pos, MonobankBlockEntity monobankEntity) {
        if (level.isClientSide)
            return InteractionResult.SUCCESS;

        boolean locked = monobankEntity.isLocked();
        if (locked) {
            player.displayClientMessage(TextUtil.translate("interaction.message.locking.monobank_is_locked"), true);
            level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_NETHERITE, SoundSource.BLOCKS,
                    1f, level.getRandom().nextFloat() * 0.02f + 0.95f);
        }
        else if (player instanceof ServerPlayer serverPlayer)
            NetworkHooks.openGui(serverPlayer, monobankEntity, pos);

        return InteractionResult.CONSUME;
    }

    private InteractionResult tryLockOrUnlock(BlockState blockState, Player player, Level level, BlockPos pos, MonobankBlockEntity monobankEntity) {
        if (level.isClientSide)
            return InteractionResult.SUCCESS;

        boolean isPublic = !monobankEntity.hasOwner();

        if (isPublic) { // Cannot lock public bank
            player.displayClientMessage(TextUtil.translate("interaction.message.locking.cannot_lock_public_bank"), true);
            level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_NETHERITE, SoundSource.BLOCKS,
                    0.8f, level.getRandom().nextFloat() * 0.1f + 0.9f);
            return InteractionResult.CONSUME;
        }

        boolean locked = monobankEntity.isLocked();
        boolean isOwner = monobankEntity.isOwnedBy(player);

        if (isOwner) { // Lock/Unlock to the heart's content
            boolean newLockedValue = !locked;
            monobankEntity.setLocked(newLockedValue);
            // ^ only setting it server-side - so we need to update clients as well(otherwise door open/closing will not render):
            level.sendBlockUpdated(pos, blockState, blockState, Block.UPDATE_ALL);
            player.displayClientMessage(TextUtil.translate("interaction.message.locking." +
                    (newLockedValue ? "locked" : "unlocked")), true);

            level.playSound(null, pos, SoundEvents.LANTERN_BREAK, SoundSource.BLOCKS,
                    0.8f, level.getRandom().nextFloat() * 0.1f + 0.9f);
            return InteractionResult.CONSUME;
        }

        // Not owner:

        if (locked) { // Try to unlock with code (future)
            // TODO: unlocking screen with item code
            player.displayClientMessage(TextUtil.translate("interaction.message.locking.cannot_unlock_not_owner"), true);
            level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_NETHERITE, SoundSource.BLOCKS,
                    1f, level.getRandom().nextFloat() * 0.1f + 0.9f);
        }
        else { // Cannot lock another owner's bank:
            player.displayClientMessage(TextUtil.translate("interaction.message.locking.cannot_lock_not_owner"), true);
            level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_NETHERITE, SoundSource.BLOCKS,
                    1f, level.getRandom().nextFloat() * 0.1f + 0.9f);
        }

        return InteractionResult.CONSUME;
    }

}
