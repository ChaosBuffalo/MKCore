package com.chaosbuffalo.mkcore.abilities;

import com.mojang.serialization.Codec;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public enum AbilitySource implements IStringSerializable {
    TRAINED(true, true),
    GRANTED(true, false),
    TALENT(false, false),
    ITEM(false, false),
    ADMIN(false, false);

    public static final Codec<AbilitySource> CODEC = IStringSerializable.createEnumCodec(AbilitySource::values, AbilitySource::valueOf);

    private final boolean placeOnBarWhenLearned;
    private final boolean useAbilityPool;

    AbilitySource(boolean placeOnBarWhenLearned, boolean useAbilityPool) {
        this.placeOnBarWhenLearned = placeOnBarWhenLearned;
        this.useAbilityPool = useAbilityPool;
    }

    public boolean placeOnBarWhenLearned() {
        return placeOnBarWhenLearned;
    }

    public boolean usesAbilityPool() {
        return useAbilityPool;
    }

    @Nonnull
    @Override
    public String getString() {
        return name();
    }
}
