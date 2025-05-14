package io.github.mortuusars.monobank.world.block.monobank.component;

import com.google.common.base.Preconditions;
import io.github.mortuusars.monobank.Monobank;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        Preconditions.checkElementIndex(slot, combination.size());
        return combination.get(slot).equals(key);
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
        for (Item item : combination) {
            ResourceLocation location = BuiltInRegistries.ITEM.getKey(item);
            StringTag stringTag = StringTag.valueOf(location.toString());
            list.add(stringTag);
        }
        return list;
    }

    public void deserializeNBT(ListTag listTag) {
        int listTagSize = listTag.size();
        for (int i = 0; i < listTagSize; i++) {
            String itemRegistryName = listTag.getString(i);
            ResourceLocation parse = ResourceLocation.parse(itemRegistryName);
            Item item = BuiltInRegistries.ITEM.get(parse);
            combination.set(i, item);
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
        for (Item item : combination) {
            buffer.writeUtf(BuiltInRegistries.ITEM.getKey(item).toString());
        }
    }

    public static Combination fromBuffer(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        List<Item> combination = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            combination.add(findItemById(buffer.readUtf()));
        }
        return new Combination(combination);
    }

    @Override
    public String toString() {
        return "Combination:[" + combination.stream().map(Item::toString).collect(Collectors.joining(",")) + "]";
    }

    protected static @NotNull Item findItemById(String location) {
        @Nullable Item item = null;
        try {
            item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(location));
        } catch (Exception e) {
            Monobank.LOGGER.error("Unknown item in combination: {}", location);
        }
        return item != null ? item : Items.AIR;
    }
}
