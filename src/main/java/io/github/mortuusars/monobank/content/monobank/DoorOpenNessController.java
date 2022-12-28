package io.github.mortuusars.monobank.content.monobank;

import net.minecraft.util.Mth;

/**
 * Used to control Monobank door position.
 */
public class DoorOpenNessController {
    private final float step;
    private final float unlockedOpenNess;

    private boolean locked;
    private boolean shouldBeOpen;
    private float openness;
    private float prevOpenness;

    /**
     * @param step Controls how much openness will increase each tick.
     * @param unlockedOpenNess Openness position of an UNLOCKED door. 0.0 - 1.0.
     */
    public DoorOpenNessController(float step, float unlockedOpenNess) {

        if (unlockedOpenNess < 0.0f || unlockedOpenNess > 1.0f)
            throw new IllegalArgumentException("'unlockedOpenNess' should be in range 0.0 - 1.0. Value: '" + unlockedOpenNess + "'.");

        this.step = step;
        this.unlockedOpenNess = unlockedOpenNess;
    }

    public void tickDoor() {
        this.prevOpenness = this.openness;

        if (locked && openness <= 0.0f) // Closed
            return;

        if (locked && openness > 0.0f) // Close
            this.openness = Math.max(this.openness - step, 0.0F);
        else if (!locked && !shouldBeOpen) { // Slightly opened
            openness = openness > unlockedOpenNess ?
                    Math.max(this.openness - step, unlockedOpenNess) :
                    Math.min(this.openness + step, unlockedOpenNess);
        }
        else if (shouldBeOpen) // Open fully
            openness = Math.min(this.openness + step, 1.0f);
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
