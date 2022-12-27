package io.github.mortuusars.monobank.content.monobank;

public enum ScreenKeyModifier {
    NONE,
    SHIFT,
    CTRL,
    SHIFT_AND_CTRL;

    public int getID() {
        if (this == ScreenKeyModifier.SHIFT)
            return 1;

        if (this == ScreenKeyModifier.CTRL)
            return 2;

        if (this == ScreenKeyModifier.SHIFT_AND_CTRL)
            return 3;

        return 0;
    }

    public static ScreenKeyModifier fromID(int id) {
        ScreenKeyModifier[] values = ScreenKeyModifier.values();
        if (id >= values.length)
            throw new IllegalArgumentException("'id' is out of bounds for this enum: '" + id + "'.");
        return values[id];
    }
}
