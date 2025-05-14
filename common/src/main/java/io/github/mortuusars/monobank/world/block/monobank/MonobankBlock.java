package io.github.mortuusars.monobank.world.block.monobank;

import com.mojang.authlib.GameProfile;
import io.github.mortuusars.monobank.Config;
import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.util.NumberFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class MonobankBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public MonobankBlock() {
        super(Properties.of()
                .mapColor(MapColor.COLOR_BLACK)
                .strength(8F, 1200F)
                .noOcclusion()
                .sound(SoundType.NETHERITE_BLOCK));

        registerDefaultState(this.getStateDefinition().any()
                .setValue(FACING, Direction.NORTH));
    }

    // --

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (!Config.Server.CAN_RELOCATE_OTHER_PLAYERS_BANK.get()
            && level.getBlockEntity(pos) instanceof MonobankBlockEntity monobankEntity
            && monobankEntity.getOwner().isPlayerOwned()
            && !monobankEntity.getOwner().isOwnedBy(player))
                return 0f; // Indestructible

        return super.getDestroyProgress(state, player, level, pos);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return Monobank.BlockEntityTypes.MONOBANK.get().create(pos, state);
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
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof MonobankBlockEntity monobankEntity && !monobankEntity.getLock().isLocked()) {
            float fullness = monobankEntity.getFullness();
            return Mth.clamp((int) Math.floor(fullness * 14.0f), 0, 14) + (fullness > 0.0f ? 1 : 0);
        }
        return 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        MonobankBlockEntity.appendHoverText(stack, context, components, flag);
    }

    //TODO: test if unpacking loot table is needed.
//    @Override
//    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
//        super.playerDestroy(level, player, pos, state, blockEntity, tool);
//    }
//
//    @Override
//    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
//        if (!level.isClientSide && level.getBlockEntity(pos) instanceof MonobankBlockEntity monobankEntity) {
//            monobankEntity.unpackLootTable(player, true);
//            monobankEntity.checkAndPunishForCrime(player, Thief.Offence.HEAVY);
//        }
//        super.playerWillDestroy(level, pos, state, player);
//    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!(level.getBlockEntity(pos) instanceof MonobankBlockEntity monobankBlockEntity))
            return;

        if (stack.getComponents().get(DataComponents.CUSTOM_NAME) != null) {
            monobankBlockEntity.setCustomName(stack.getHoverName());
        }

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
        return entityType.equals(Monobank.BlockEntityTypes.MONOBANK.get()) && level.isClientSide ?
               MonobankBlockEntity::clientTick : MonobankBlockEntity::serverTick;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof MonobankBlockEntity monobankBlockEntity)) {
            return InteractionResult.FAIL;
        }

        // Testing
        if (Monobank.IN_DEBUG) {
            if (player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.WOODEN_PICKAXE)) {
                monobankBlockEntity.breakInAttempted = true;
                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            if (player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.IRON_PICKAXE)) {
                monobankBlockEntity.breakInSucceeded = true;
                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            if (player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.MILK_BUCKET)) {
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
           | Unlocked:  |   Lock, Open  |      Lock, Open      |
           |____________|_______________|______________________| */

        return player.isSecondaryUseActive() ?
                tryLockOrUnlock(state, player, level, pos, monobankBlockEntity) :
                tryOpen(state, player, level, pos, monobankBlockEntity);
    }

    private InteractionResult tryOpen(BlockState state, Player player, Level level, BlockPos pos, MonobankBlockEntity monobankEntity) {
        if (monobankEntity.getLock().isLocked()) { // Should be unlocked first
            if (level.isClientSide) {
                String hotkey = Minecraft.getInstance().options.keyShift.getTranslatedKeyMessage().getString(999) +
                        "+" + Minecraft.getInstance().options.keyUse.getTranslatedKeyMessage().getString(999);
                player.displayClientMessage(Component.translatable("monobank.message.monobank.locking.monobank_is_locked", hotkey), true);
                return InteractionResult.SUCCESS;
            }
            else {
                monobankEntity.playSoundAtDoor(Monobank.SoundEvents.MONOBANK_CLICK.get());
            }
        }
        else if (player instanceof ServerPlayer serverPlayer) {
            monobankEntity.open(serverPlayer);
        }

        return InteractionResult.CONSUME;
    }

    private InteractionResult tryLockOrUnlock(BlockState blockState, Player player, Level level, BlockPos pos, MonobankBlockEntity monobankEntity) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        boolean isLocked = monobankEntity.getLock().isLocked();

        if (Config.Server.OWNER_CAN_UNLOCK_WITHOUT_COMBINATION.get() && monobankEntity.getOwner().isOwnedBy(player)) { // Lock/Unlock to the heart's content
            boolean shouldBeLocked = !isLocked;

            if (shouldBeLocked) {
                monobankEntity.getLock().setLocked(true);
                return InteractionResult.CONSUME;
            }

            if (monobankEntity.isUnlocking())
                player.displayClientMessage(Component.translatable("monobank.message.monobank.unlocking"), true);
            else
                monobankEntity.startUnlocking(player);

            return InteractionResult.CONSUME;
        }

        // Not owner:

        if (isLocked) { // Try to unlock with code
            if (player instanceof ServerPlayer serverPlayer)
                monobankEntity.openUnlockingGui(serverPlayer);

            MonobankBlockEntity.playSoundAtDoor(level, pos, blockState, Monobank.SoundEvents.MONOBANK_CLICK.get());
        }
        else { // Lock
            monobankEntity.getLock().setLocked(true);
            MonobankBlockEntity.playSoundAtDoor(level, pos, blockState, Monobank.SoundEvents.MONOBANK_CLICK.get());
        }

        return InteractionResult.CONSUME;
    }
}
