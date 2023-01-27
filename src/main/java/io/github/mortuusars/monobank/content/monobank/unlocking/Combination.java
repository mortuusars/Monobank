package io.github.mortuusars.monobank.content.monobank.unlocking;

import com.google.common.base.Preconditions;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines item combination which is needed to unlock the Monobank.
 */
public class Combination {
    public static final int SIZE = 3;
    private final List<Item> combination;

    public Combination(List<Item> combination) {
        Preconditions.checkArgument(combination.size() == SIZE,
                "Combination should have " + SIZE + " items. Provided " + combination.size() + ".");
        this.combination = new ArrayList<>(combination);
    }

    public static Combination empty() {
        return new Combination(List.of(Items.AIR, Items.AIR, Items.AIR));
    }

    public boolean isEmpty() {
        return combination.stream().allMatch(item -> item == Items.AIR);
    }

    public Item getItemInSlot(int slot) {
        Preconditions.checkState(slot >= 0 && slot < Combination.SIZE, "Slot is out of bounds.");
        return combination.get(slot);
    }

    /**
     * Checks if the provided sequence are matching the combination in correct order.
     */
    public boolean matches(List<Item> sequence) {
        if (sequence.size() < SIZE)
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
    public boolean matches(int slot, Item key) {
        if (slot < 0 || slot >= combination.size())
            return false;

        return ForgeRegistries.ITEMS.getKey(combination.get(slot)).equals(ForgeRegistries.ITEMS.getKey(key));
    }

    /**
     * Finds combination slot matching the key or -1 if none found.
     */
    public int findMatchingSlot(Item key) {
        for (int i = 0; i < combination.size(); i++) {
            if (matches(i, key))
                return i;
        }
        return -1;
    }

    public ListTag serializeNBT() {
        ListTag list = new ListTag();
        for (int i = 0; i < combination.size(); i++) {
            StringTag stringTag = StringTag.valueOf(ForgeRegistries.ITEMS.getKey(combination.get(i)).toString());
            list.add(stringTag);
        }
        return list;
    }

    public void deserializeNBT(ListTag listTag) {
        int listTagSize = listTag.size();
        for (int i = 0; i < listTagSize; i++) {
            String itemRegistryName = listTag.getString(i);
            @Nullable Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemRegistryName));
            combination.set(i, item != null ? item : Items.AIR);
        }

        if (listTagSize < SIZE) {
            // Add blanks to the end:
            for (int i = listTagSize; i < SIZE - listTagSize; i++) {
                combination.set(i, Items.AIR);
            }
        }
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(combination.size());
        for (int i = 0; i < combination.size(); i++)
            buffer.writeUtf(ForgeRegistries.ITEMS.getKey(combination.get(i)).toString());
    }

    public static Combination fromBuffer(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        List<Item> combination = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            combination.add(fromRegistryName(buffer.readUtf()));
        }
        return new Combination(combination);
    }

    @Override
    public String toString() {
        return "Combination:[" + String.join(",", combination.stream().map(item ->
                item.toString()).collect(Collectors.toList())) + "]";
    }

    private static Item fromRegistryName(String itemRegistryName) {
        @Nullable Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemRegistryName));
        return item != null ? item : Items.AIR;
    }
}
