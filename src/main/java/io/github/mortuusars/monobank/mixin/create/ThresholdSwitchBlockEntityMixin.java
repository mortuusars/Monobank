package io.github.mortuusars.monobank.mixin.create;

import com.simibubi.create.content.redstone.thresholdSwitch.ThresholdSwitchBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import io.github.mortuusars.monobank.config.Configuration;
import io.github.mortuusars.monobank.content.monobank.MonobankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ThresholdSwitchBlockEntity.class, remap = false)
public abstract class ThresholdSwitchBlockEntityMixin extends SmartBlockEntity {
    @Shadow protected abstract BlockPos getTargetPos();

    @Shadow public int currentMaxLevel;

    @Shadow public int currentLevel;

    public ThresholdSwitchBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "updateCurrentLevel", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/inventory/TankManipulationBehaviour;hasInventory()Z"))
    private void updateLevel(CallbackInfo ci) {
        if (level != null && level.getBlockEntity(getTargetPos()) instanceof MonobankBlockEntity monobankBlockEntity) {
            // Without this, Threshold Switch will show only 64 items.
            currentLevel = monobankBlockEntity.getStoredItemStack().getCount();
            currentMaxLevel = Configuration.MONOBANK_CAPACITY.get();
        }
    }
}
