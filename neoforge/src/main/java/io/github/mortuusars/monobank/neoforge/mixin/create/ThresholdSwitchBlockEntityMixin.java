package io.github.mortuusars.monobank.neoforge.mixin.create;

import com.simibubi.create.content.redstone.thresholdSwitch.ThresholdSwitchBlockEntity;
import io.github.mortuusars.monobank.world.block.monobank.MonobankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThresholdSwitchBlockEntity.class)
public abstract class ThresholdSwitchBlockEntityMixin extends BlockEntity {
    @Shadow protected abstract BlockPos getTargetPos();

    @Shadow public int currentMaxLevel;

    public ThresholdSwitchBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Inject(method = "updateCurrentLevel", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/inventory/TankManipulationBehaviour;hasInventory()Z"))
    private void updateCurrentLevel(CallbackInfo ci) {
        if (level != null && level.getBlockEntity(getTargetPos()) instanceof MonobankBlockEntity monobankBlockEntity) {
            // Without this, Threshold Switch will show only 64 items.
            currentMaxLevel = monobankBlockEntity.getCapacity();
        }
    }
}
