package io.github.mortuusars.monobank.content.monobank.component;


public enum WithdrawAction {
    SINGLE_ITEM,
    STACK,
    ALL;

    public static WithdrawAction fromKeyModifier(ScreenKeyModifier keyModifier) {
        if (keyModifier == ScreenKeyModifier.SHIFT)
            return WithdrawAction.STACK;

        if (keyModifier == ScreenKeyModifier.SHIFT_AND_CTRL)
            return WithdrawAction.ALL;

        return WithdrawAction.SINGLE_ITEM;
    }
}
