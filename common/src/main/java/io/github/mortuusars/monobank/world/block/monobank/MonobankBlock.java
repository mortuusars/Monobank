package io.github.mortuusars.monobank.world.block.monobank;

import com.mojang.authlib.GameProfile;
import io.github.mortuusars.monobank.Config;
import io.github.mortuusars.monobank.Monobank;
import io.github.mortuusars.monobank.PlatformHelper;
import io.github.mortuusars.monobank.client.util.ClientUtil;
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
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class MonobankBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public MonobankBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    // --

    public boolean canUnlockWithoutCombination(Player player, MonobankBlockEntity blockEntity) {
        if (Config.Server.ANYONE_CAN_UNLOCK_WITHOUT_COMBINATION.get()) return true;
        return Config.Server.OWNER_CAN_UNLOCK_WITHOUT_COMBINATION.get()
                && blockEntity.getOwner().isOwnedBy(player);
    }

    public boolean canBreak(Player player, BlockPos pos, BlockState state) {
        if (Config.Server.CAN_RELOCATE_OTHER_PLAYERS_BANK.get()) return true;
        return player.level().getBlockEntity(pos) instanceof MonobankBlockEntity monobankEntity
                && monobankEntity.getOwner().isPlayerOwned()
                && monobankEntity.getOwner().isOwnedBy(player);
    }

    // -- Use

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof MonobankBlockEntity blockEntity)) {
            return InteractionResult.FAIL;
        }

        if (PlatformHelper.isInDevEnv()) {
            InteractionResult result = handleDevEnvUse(level, pos, player, blockEntity);
            if (result != InteractionResult.PASS) {
                return result;
            }
        }

        if (player.isSecondaryUseActive()) {
            return blockEntity.getLock().isLocked()
                    ? tryUnlock(player, blockEntity)
                    : lock(player, blockEntity);
        }

        return tryOpen(player, blockEntity);
    }

    private InteractionResult handleDevEnvUse(Level level, BlockPos pos, Player player, MonobankBlockEntity blockEntity) {
        if (player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.WOODEN_PICKAXE)) {
            blockEntity.breakInAttempted = true;
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.IRON_PICKAXE)) {
            blockEntity.breakInSucceeded = true;
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.MILK_BUCKET)) {
            blockEntity.setOwner(new Player(level, pos, 0, new GameProfile(UUID.randomUUID(), "John")) {
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

        return InteractionResult.PASS;
    }

    protected InteractionResult tryUnlock(Player player, MonobankBlockEntity blockEntity) {
        if (!blockEntity.getLock().isLocked()) return InteractionResult.FAIL;
        if (player.level().isClientSide) return InteractionResult.SUCCESS;

        if (blockEntity.getLock().isUnlocking()) {
            player.displayClientMessage(Component.translatable("monobank.message.monobank.unlocking"), true);
            return InteractionResult.SUCCESS;
        }

        if (canUnlockWithoutCombination(player, blockEntity)) {
            blockEntity.startUnlocking(player);
            return InteractionResult.SUCCESS;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            blockEntity.openUnlockingGui(serverPlayer);
            blockEntity.playSoundAtDoor(Monobank.SoundEvents.MONOBANK_CLICK.get());
        }

        return InteractionResult.SUCCESS;
    }

    protected InteractionResult lock(Player player, MonobankBlockEntity blockEntity) {
        blockEntity.getLock().setLocked(true);
        blockEntity.playSoundAtDoor(Monobank.SoundEvents.MONOBANK_CLICK.get());
        return InteractionResult.SUCCESS;
    }

    protected InteractionResult tryOpen(Player player, MonobankBlockEntity blockEntity) {
        if (blockEntity.getLock().isLocked()) {
            if (player.level().isClientSide) {
                String sneakUseKey = ClientUtil.getSneakUseKeyTranslation();
                player.displayClientMessage(Component.translatable(
                        "monobank.message.monobank.locking.monobank_is_locked", sneakUseKey), true);
            } else {
                blockEntity.playSoundAtDoor(Monobank.SoundEvents.MONOBANK_CLICK.get());
            }
            return InteractionResult.SUCCESS;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            blockEntity.open(serverPlayer);
        }

        return InteractionResult.SUCCESS;
    }

    // -- Block Entity

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return Monobank.BlockEntityTypes.MONOBANK.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
        return entityType.equals(Monobank.BlockEntityTypes.MONOBANK.get()) && level.isClientSide
                ? MonobankBlockEntity::clientTick
                : MonobankBlockEntity::serverTick;
    }

    // --

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!(level.getBlockEntity(pos) instanceof MonobankBlockEntity monobankBlockEntity))
            return;

        if (stack.getComponents().get(DataComponents.CUSTOM_NAME) != null) {
            monobankBlockEntity.setCustomName(stack.getHoverName());
        }

        monobankBlockEntity.onSetPlacedBy(placer, stack);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (canBreak(player, pos, state)) {
            return 0f; // Indestructible
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public @NotNull BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof MonobankBlockEntity monobankEntity) {
            //TODO: test if unpacking loot table is needed.
            // monobankEntity.unpackLootTable(player, true);

            //TODO: Crime for breaking
            // monobankEntity.checkAndPunishForCrime(player, Thief.Offence.HEAVY);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    // -- State

    @Override
    public @NotNull RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    // -- Redstone

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

    // --

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        /*                CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("BlockEntityTag", CompoundTag.TAG_COMPOUND)) {
            CompoundTag blockEntityTag = tag.getCompound("BlockEntityTag");
            if (blockEntityTag.contains(LOCK_TAG, CompoundTag.TAG_COMPOUND)) {
                CompoundTag lockTag = blockEntityTag.getCompound(LOCK_TAG);
                boolean locked = lockTag.getBoolean("Locked");
                if (locked)
                    tooltip.add(TextUtil.translate("tooltip.locked").withStyle(ChatFormatting.GRAY));

                if (blockEntityTag.contains(LOOT_TABLE_TAG, CompoundTag.TAG_STRING)) {
                    String lootTable = blockEntityTag.getString(LOOT_TABLE_TAG);
                    tooltip.add(TextUtil.translate("tooltip.loot_table", lootTable)
                            .withStyle(ChatFormatting.DARK_GRAY));
                }

                if (lockTag.contains("CombinationTable", CompoundTag.TAG_STRING)) {
                    tooltip.add(TextUtil.translate("tooltip.combination_table", lockTag.getString("CombinationTable"))
                            .withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }*/
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        super.triggerEvent(state, level, pos, id, param);
        // Trigger door openers counter to recheck:
        BlockEntity blockentity = level.getBlockEntity(pos);
        return blockentity != null && blockentity.triggerEvent(id, param);
    }
}
