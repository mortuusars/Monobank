package io.github.mortuusars.monobank.world.block.monobank.component;

import com.google.common.base.Preconditions;
import io.github.mortuusars.monobank.Monobank;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.OptionalInt;
import java.util.stream.Stream;

/**
 * Defines item combination which is needed to unlock the Monobank.
 */
public record Combination(Item first, Item second, Item third) implements Iterable<Item> {
    public static final int SIZE = 3;
    public static final Combination EMPTY = new Combination(Items.AIR, Items.AIR, Items.AIR);

    public Combination(ItemStack first, ItemStack second, ItemStack third) {
        this(first.getItem(), second.getItem(), third.getItem());
    }

    public Item getItem(int slot) {
        Preconditions.checkElementIndex(slot, 3);
        return switch (slot) {
            case 0 -> first();
            case 1 -> second();
            case 2 -> third();
            default -> throw new IllegalStateException("Unexpected value: " + slot);
        };
    }

    public boolean isEmpty() {
        return first.equals(Items.AIR) && second.equals(Items.AIR) && third.equals(Items.AIR);
    }

    // -- Match

    public boolean matches(int slot, Item key) {
        Item item = getItem(slot);
        return item.equals(Items.AIR) || item.equals(key);
    }

    public boolean matches(int slot, ItemStack key) {
        return matches(slot, key.getItem());
    }

    public boolean matches(Container container) {
        if (isEmpty()) return true;

        for (int i = 0; i < 3; i++) {
            if (container.getContainerSize() - 1 < i) {
                if (getItem(i).equals(Items.AIR)) {
                    continue;
                } else {
                    return false;
                }
            }

            if (!matches(i, container.getItem(i).getItem())) {
                return false;
            }
        }

        return true;
    }

    // -- Save / Load

    public static Combination load(ListTag listTag) {
        return new Combination(
                findItemById(listTag.getString(0)),
                findItemById(listTag.getString(1)),
                findItemById(listTag.getString(2)));
    }

    public ListTag save() {
        ListTag list = new ListTag();
        for (Item item : this) {
            ResourceLocation location = BuiltInRegistries.ITEM.getKey(item);
            StringTag stringTag = StringTag.valueOf(location.toString());
            list.add(stringTag);
        }
        return list;
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(BuiltInRegistries.ITEM.getKey(first).toString());
        buffer.writeUtf(BuiltInRegistries.ITEM.getKey(second).toString());
        buffer.writeUtf(BuiltInRegistries.ITEM.getKey(third).toString());
    }

    public static Combination fromBuffer(FriendlyByteBuf buffer) {
        return new Combination(
                findItemById(buffer.readUtf()),
                findItemById(buffer.readUtf()),
                findItemById(buffer.readUtf()));
    }

    // --

    @Override
    public String toString() {
        return "Combination:[" + first.toString() + ", " + second.toString() + ", " + third.toString() + "]";
    }

    // -- Iterable

    @Override
    public @NotNull Iterator<Item> iterator() {
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < SIZE;
            }

            @Override
            public Item next() {
                if (!hasNext()) throw new NoSuchElementException();
                return switch (index++) {
                    case 0 -> first;
                    case 1 -> second;
                    case 2 -> third;
                    default -> throw new IllegalStateException();
                };
            }
        };
    }

    public Stream<Item> stream() {
        return Stream.of(first, second, third);
    }

    // -- Util

    /**
     * Finds combination slot matching the key or -1 if none found.
     */
    public OptionalInt findMatchingSlot(Item key) {
        for (int i = 0; i < SIZE; i++) {
            if (matches(i, key)) {
                return OptionalInt.of(i);
            }
        }
        return OptionalInt.empty();
    }

    private static @NotNull Item findItemById(String location) {
        if (location.isBlank()) {
            return Items.AIR;
        }
        try {
            return BuiltInRegistries.ITEM.get(ResourceLocation.parse(location));
        } catch (Exception e) {
            Monobank.LOGGER.error("Unknown item in combination: {}", location);
            return Items.AIR;
        }
    }
}
