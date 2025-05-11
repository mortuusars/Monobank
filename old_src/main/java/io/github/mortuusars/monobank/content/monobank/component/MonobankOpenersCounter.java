package io.github.mortuusars.monobank.content.monobank.component;

import io.github.mortuusars.monobank.Registry;
import io.github.mortuusars.monobank.content.monobank.MonobankBlockEntity;
import io.github.mortuusars.monobank.content.monobank.MonobankMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;

public class MonobankOpenersCounter extends ContainerOpenersCounter {
    public static final int UPDATE_DOOR_EVENT_ID = 1;
    private final MonobankBlockEntity monobankBlockEntity;

    public MonobankOpenersCounter(MonobankBlockEntity monobankBlockEntity) {
        this.monobankBlockEntity = monobankBlockEntity;
    }

    @Override
    protected void onOpen(Level level, BlockPos pos, BlockState state) {
        MonobankBlockEntity.playSoundAtDoor(level, pos, state, Registry.Sounds.MONOBANK_OPEN.get());
    }

    @Override
    protected void onClose(Level level, BlockPos pos, BlockState state) {
        MonobankBlockEntity.playSoundAtDoor(level, pos, state, Registry.Sounds.MONOBANK_CLOSE.get());
    }

    @Override
    protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int count, int openCount) {
        // Send update to client:
        level.blockEvent(pos, state.getBlock(), UPDATE_DOOR_EVENT_ID, openCount);
    }

    @Override
    protected boolean isOwnContainer(Player player) {
        return player.containerMenu instanceof MonobankMenu monobankMenu && monobankMenu.getBlockEntity() == monobankBlockEntity;
    }
}
