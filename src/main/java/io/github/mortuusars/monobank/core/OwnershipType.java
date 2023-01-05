package io.github.mortuusars.monobank.core;


import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public enum OwnershipType implements StringRepresentable {
    NONE("none"),
    PUBLIC("public"),
    PLAYER("player");

    private final String name;
    private static final Map<String, OwnershipType> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(OwnershipType::getSerializedName, ownershipType -> ownershipType));

    OwnershipType(String name) {
        this.name = name;
    }

    public static OwnershipType byName(String name) {
        if (name == null)
            throw new IllegalArgumentException("'name' cannot be null.");
        return BY_NAME.get(name.toLowerCase(Locale.ROOT));
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
