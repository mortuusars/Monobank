package io.github.mortuusars.monobank.content.monobank.unlocking;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Combination {
    private static String FIRST_TAG_ID = "First";
    private static String SECOND_TAG_ID = "Second";
    private static String THIRD_TAG_ID = "Third";

    private final ArrayList<ItemStack> combination;

    public Combination(ArrayList<ItemStack> combination) {
        this.combination = combination;
    }
    public Combination(ItemStack first, ItemStack second, ItemStack third) {
        this(new ArrayList(List.of(first, second, third)));
    }
    public Combination(Item first, Item second, Item third) {
        this(new ItemStack(first), new ItemStack(second), new ItemStack(third));
    }

    public static Combination empty() {
        return new Combination(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
    }

    public ItemStack getFirst() { return combination.get(0); }
    public ItemStack getSecond() { return combination.get(1); }
    public ItemStack getThird() { return combination.get(2); }

    public boolean isEmpty() {
        return getFirst().isEmpty() && getSecond().isEmpty() && getThird().isEmpty();
    }

    /**
     * Checks if the provided sequence are matching the combination in correct order.
     */
    public boolean matches(List<ItemStack> sequence) {
        if (sequence.size() < 3)
            return false;

        for (int i = 0; i < combination.size(); i++) {
            if (!matches(i, sequence.get(i)))
                return false;
        }

        return true;
    }

    /**
     * Checks if the key matches specified combination slot.
     */
    public boolean matches(int slot, ItemStack key) {
        if (slot < 0 || slot >= combination.size())
            return false;

        return combination.get(slot).getItem().getRegistryName().equals(key.getItem().getRegistryName());
    }

    /**
     * Finds combination slot matching the key or -1 if none found.
     */
    public int findMatchingSlot(ItemStack key) {
        for (int i = 0; i < combination.size(); i++) {
            if (matches(i, key))
                return i;
        }
        return -1;
    }

    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString(FIRST_TAG_ID, getFirst().getItem().getRegistryName().toString());
        compoundTag.putString(SECOND_TAG_ID, getSecond().getItem().getRegistryName().toString());
        compoundTag.putString(THIRD_TAG_ID, getThird().getItem().getRegistryName().toString());
        return compoundTag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(FIRST_TAG_ID)) {
            @Nullable Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag.getString(FIRST_TAG_ID)));
            combination.set(0, item != null ? new ItemStack(item) : ItemStack.EMPTY);
        }
        if (tag.contains(SECOND_TAG_ID)) {
            @Nullable Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag.getString(SECOND_TAG_ID)));
            combination.set(1, item != null ? new ItemStack(item) : ItemStack.EMPTY);
        }
        if (tag.contains(THIRD_TAG_ID)) {
            @Nullable Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag.getString(THIRD_TAG_ID)));
            combination.set(2, item != null ? new ItemStack(item) : ItemStack.EMPTY);
        }
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeItem(getFirst());
        buffer.writeItem(getSecond());
        buffer.writeItem(getThird());
    }

    public static Combination fromBuffer(FriendlyByteBuf buffer) {
        return new Combination(buffer.readItem(), buffer.readItem(), buffer.readItem());
    }

    @Override
    public String toString() {
        return "{First:'" + getFirst().getItem().getRegistryName() +
                "',Second:'" + getSecond().getItem().getRegistryName() +
                "',Third:'" + getThird().getItem().getRegistryName() + "'}";
    }
}
