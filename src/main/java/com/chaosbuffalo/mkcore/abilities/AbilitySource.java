package com.chaosbuffalo.mkcore.abilities;

import com.mojang.serialization.Codec;
import net.minecraft.util.IStringSerializable;

public enum AbilitySource implements IStringSerializable {
    TRAINED(true),
    TALENT(false),
    ITEM(false),
    ADMIN(false);

    public static final Codec<AbilitySource> CODEC = IStringSerializable.createEnumCodec(AbilitySource::values, AbilitySource::valueOf);

    private final boolean placeOnBarWhenLearned;

    AbilitySource(boolean placeOnBarWhenLearned) {
        this.placeOnBarWhenLearned = placeOnBarWhenLearned;
    }

    public boolean placeOnBarWhenLearned() {
        return placeOnBarWhenLearned;
    }

    @Override
    public String getString() {
        return name();
    }
}
