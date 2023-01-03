package io.github.mortuusars.monobank.content.monobank;

import io.github.mortuusars.monobank.Registry;
import io.github.mortuusars.monobank.util.TextUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
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
        return Registry.BlockEntityTypes.MONOBANK.get().create(pos, state);
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

    // Used to trigger door openers counter to recheck. Can be used for other purposes too.
    @Override
    public boolean triggerEvent(BlockState pState, Level pLevel, BlockPos pPos, int pId, int pParam) {
        super.triggerEvent(pState, pLevel, pPos, pId, pParam);
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        return blockentity != null && blockentity.triggerEvent(pId, pParam);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
        return entityType == Registry.BlockEntityTypes.MONOBANK.get() && level.isClientSide ?
               MonobankBlockEntity::clientTick : MonobankBlockEntity::serverTick;
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

        if (monobankEntity.isLocked()) { // Should be unlocked first
            player.displayClientMessage(TextUtil.translate("interaction.message.locking.monobank_is_locked"), true);
            MonobankBlockEntity.playSoundAtDoor(level, pos, blockState, Registry.Sounds.MONOBANK_CLICK.get());
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
            MonobankBlockEntity.playSoundAtDoor(level, pos, blockState, Registry.Sounds.MONOBANK_CLICK.get());
            return InteractionResult.CONSUME;
        }

        boolean isLocked = monobankEntity.isLocked();
        boolean isOwner = monobankEntity.isOwnedBy(player);

//        if (isLocked) {
//            if (player instanceof ServerPlayer serverPlayer)
//                NetworkHooks.openGui(serverPlayer, monobankEntity, pos);
//
//            return InteractionResult.SUCCESS;
//        }

        if (isOwner) { // Lock/Unlock to the heart's content
            boolean shouldBeLocked = !isLocked;

            if (shouldBeLocked) {
                monobankEntity.lock();
                return InteractionResult.CONSUME;
            }

            if (monobankEntity.isUnlocking())
                player.displayClientMessage(TextUtil.translate("interaction.message.unlocking"), true);
            else
                monobankEntity.startUnlocking(level.getRandom().nextInt(20, 61));

            return InteractionResult.CONSUME;
        }

        // Not owner:

        if (isLocked) { // Try to unlock with code (future)
            // TODO: unlocking screen with item code
            player.displayClientMessage(TextUtil.translate("interaction.message.locking.cannot_unlock_not_owner"), true);
            MonobankBlockEntity.playSoundAtDoor(level, pos, blockState, Registry.Sounds.MONOBANK_CLICK.get());
        }
        else { // Cannot lock another owner's bank:
            player.displayClientMessage(TextUtil.translate("interaction.message.locking.cannot_lock_not_owner"), true);
            MonobankBlockEntity.playSoundAtDoor(level, pos, blockState, Registry.Sounds.MONOBANK_CLICK.get());
        }

        return InteractionResult.CONSUME;
    }
}
