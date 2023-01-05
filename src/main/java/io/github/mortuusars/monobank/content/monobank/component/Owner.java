package io.github.mortuusars.monobank.content.monobank.component;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.stream.Collectors;

public class Owner {
    private static final String TYPE_NBT_KEY = "Type";
    private static final String UUID_NBT_KEY = "UUID";

    private UUID uuid;
    private Type type;

    public Owner(UUID uuid, Type type) {
        this.uuid = uuid;
        this.type = type;
    }

    public Owner(Player player) {
        this.uuid = player.getUUID();
        this.type = Type.PLAYER;
    }

    public static Owner none() {
        return new Owner(Util.NIL_UUID, Type.NONE);
    }

    public void setOwner(Player player) {
        Objects.requireNonNull(player);
        uuid = player.getUUID();
        type = Type.PLAYER;
    }

    public void setOwner(Type type) {
        if (type == Type.PLAYER)
            throw new IllegalArgumentException("Cannot set owner to 'Type.PLAYER' without setting UUID. Use setOwner(Player player) overload.");
        this.type = type;
        this.uuid = Util.NIL_UUID;
    }

    public Type getType() {
        return type;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean hasOwner() {
        return type == Type.PLAYER || type == Type.NPC;
    }

    public boolean isPlayerOwned() {
        return type == Type.PLAYER && !uuid.equals(Util.NIL_UUID);
    }

    public boolean isOwnedBy(Player player) {
        return isPlayerOwned() && uuid.equals(player.getUUID());
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (type != Type.NONE) {
            tag.putString(TYPE_NBT_KEY, type.getSerializedName());
            if (isPlayerOwned())
                tag.putUUID(UUID_NBT_KEY, uuid);
        }
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(TYPE_NBT_KEY)) {
            type = Type.byName(tag.getString(TYPE_NBT_KEY));
            if (tag.contains(UUID_NBT_KEY))
                uuid = tag.getUUID(UUID_NBT_KEY);
        }
    }


    public enum Type implements StringRepresentable {
        NONE("none"),
        PUBLIC("public"),
        PLAYER("player"),
        NPC("npc");

        private final String name;
        private static final Map<String, Type> BY_NAME = Arrays.stream(values())
                .collect(Collectors.toMap(Type::getSerializedName, ownershipType -> ownershipType));

        Type(String name) {
            this.name = name;
        }

        public static Type byName(String name) {
            if (name == null)
                throw new IllegalArgumentException("'name' cannot be null.");
            return BY_NAME.get(name.toLowerCase(Locale.ROOT));
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
