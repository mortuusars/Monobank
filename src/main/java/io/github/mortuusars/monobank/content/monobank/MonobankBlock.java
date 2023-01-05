package io.github.mortuusars.monobank.content.monobank;

import com.mojang.authlib.GameProfile;
import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.Registry;
import io.github.mortuusars.monobank.content.monobank.component.Owner;
import io.github.mortuusars.monobank.content.monobank.unlocking.Combination;
import io.github.mortuusars.monobank.core.OwnershipType;
import io.github.mortuusars.monobank.util.TextUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

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

//        if (Monobank.IN_DEBUG && placer.getItemInHand(InteractionHand.OFF_HAND).is(Items.EMERALD)) {
////            monobankBlockEntity.setOwnershipType(OwnershipType.VILLAGE);
//            return;
//        }

        monobankBlockEntity.onSetPlacedBy(placer, stack);
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

        // Testing
        if (Monobank.IN_DEBUG) {
            if (player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.MILK_BUCKET)) {
                monobankBlockEntity.getLock().setCombination(Items.IRON_NUGGET, Items.APPLE, Items.GOLD_NUGGET);
                monobankBlockEntity.setOwner(new Player(level, pos, 0, new GameProfile(UUID.randomUUID(), "John")) {
                    @Override
                    public boolean isSpectator() {
                        return false;
                    }

                    @Override
                    public boolean isCreative() {
                        return false;
                    }
                });

                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

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

        if (monobankEntity.getLock().isLocked()) { // Should be unlocked first
            player.displayClientMessage(TextUtil.translate("interaction.message.locking.monobank_is_locked"), true);
            monobankEntity.playSoundAtDoor(Registry.Sounds.MONOBANK_CLICK.get());
        }
        else if (player instanceof ServerPlayer serverPlayer) {
            monobankEntity.open(serverPlayer);
        }

        return InteractionResult.CONSUME;
    }

    private InteractionResult tryLockOrUnlock(BlockState blockState, Player player, Level level, BlockPos pos, MonobankBlockEntity monobankEntity) {
        if (level.isClientSide)
            return InteractionResult.SUCCESS;

        if (monobankEntity.getOwner().getType() == Owner.Type.PUBLIC) { // Cannot lock public bank
            player.displayClientMessage(TextUtil.translate("interaction.message.locking.cannot_lock_public_bank"), true);
            MonobankBlockEntity.playSoundAtDoor(level, pos, blockState, Registry.Sounds.MONOBANK_CLICK.get());
            return InteractionResult.CONSUME;
        }

        boolean isLocked = monobankEntity.getLock().isLocked();

        if (monobankEntity.getOwner().isOwnedBy(player)) { // Lock/Unlock to the heart's content
            boolean shouldBeLocked = !isLocked;

            if (shouldBeLocked) {
                monobankEntity.getLock().setLocked(true);
                return InteractionResult.CONSUME;
            }

            if (monobankEntity.getLock().isUnlocking())
                player.displayClientMessage(TextUtil.translate("interaction.message.unlocking"), true);
            else
                monobankEntity.getLock().startUnlocking();

            return InteractionResult.CONSUME;
        }

        // Not owner:

        if (isLocked) { // Try to unlock with code
            if (player instanceof ServerPlayer serverPlayer)
                monobankEntity.openUnlockingGui(serverPlayer);

            MonobankBlockEntity.playSoundAtDoor(level, pos, blockState, Registry.Sounds.MONOBANK_CLICK.get());
        }
        else { // Lock
            monobankEntity.getLock().setLocked(true);
            MonobankBlockEntity.playSoundAtDoor(level, pos, blockState, Registry.Sounds.MONOBANK_CLICK.get());
        }

        return InteractionResult.CONSUME;
    }
}
