package io.github.mortuusars.monobank.content.monobank.component;

import net.minecraft.network.FriendlyByteBuf;

public class MonobankExtraInfo {
    public boolean isOwner, breakInAttempted, breakInSucceeded;

    public MonobankExtraInfo(boolean isOwner, boolean breakInAttempted, boolean breakInSucceeded) {
        this.isOwner = isOwner;
        this.breakInAttempted = breakInAttempted;
        this.breakInSucceeded = breakInSucceeded;
    }

    public boolean hasWarning() {
        return breakInAttempted || breakInSucceeded;
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeBoolean(isOwner);
        buffer.writeBoolean(breakInAttempted);
        buffer.writeBoolean(breakInSucceeded);
    }

    public static MonobankExtraInfo fromBuffer(FriendlyByteBuf buffer) {
        return new MonobankExtraInfo(buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
    }
}
