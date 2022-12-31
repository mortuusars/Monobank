package io.github.mortuusars.monobank.content.monobank.component;

import net.minecraft.util.Mth;

/**
 * Used to control Monobank door position.
 */
public class DoorOpennessController {
    private final float step = 0.1f;
    private final float lockedStepModifier, unlockedStepModifier, openStepModifier, closeStepModifier, unlockedOpenness;
    private boolean locked, shouldBeOpen;
    private float openness, prevOpenness;

    /**
     * Modifiers control the step(speed) of each moving state.
     * @param unlockedOpenness Openness position of an UNLOCKED door. 0.0 - 1.0.
     */
    public DoorOpennessController(float lockedStepModifier, float unlockedStepModifier,
                                  float openStepModifier, float closeStepModifier, float unlockedOpenness) {
        if (unlockedOpenness < 0.0f || unlockedOpenness > 1.0f)
            throw new IllegalArgumentException("'unlockedOpenness' should be in range 0.0 - 1.0. Value: '" + unlockedOpenness + "'.");

        this.lockedStepModifier = lockedStepModifier;
        this.unlockedStepModifier = unlockedStepModifier;
        this.openStepModifier = openStepModifier;
        this.closeStepModifier = closeStepModifier;

        this.unlockedOpenness = unlockedOpenness;
    }

    public void tickDoor() {
        this.prevOpenness = this.openness;

        if (locked && openness <= 0.0f) // Closed
            return;

        if (!locked && shouldBeOpen && openness >= 1.0f) // Fully open
            return;

        if (locked && openness <= unlockedOpenness) // Close from unlocked state
            this.openness = Math.max(this.openness - step * lockedStepModifier, 0.0F);
        else if (locked && openness > 0.0f) // Close fully
            this.openness = Math.max(this.openness - step * closeStepModifier, 0.0F);
        else if (!locked && !shouldBeOpen) { // Slightly opened
            openness = openness > unlockedOpenness ?
                    Math.max(this.openness - step * closeStepModifier, unlockedOpenness) :
                    Math.min(this.openness + step * unlockedStepModifier, unlockedOpenness);
        }
        else if (shouldBeOpen) // Open fully
            openness = Math.min(this.openness + step * openStepModifier, 1.0f);
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void shouldBeOpen(boolean shouldBeOpen) {
        this.shouldBeOpen = shouldBeOpen;
    }

    public float getOpenness(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevOpenness, this.openness);
    }
}
