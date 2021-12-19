package com.chaosbuffalo.mkcore.abilities;

import com.mojang.serialization.Codec;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public enum AbilitySource implements IStringSerializable {
    TRAINED(true, true, true),
    GRANTED(true, false, true),
    TALENT(false, false, false),
    ITEM(false, false, false),
    ADMIN(false, false, true);

    public static final Codec<AbilitySource> CODEC = IStringSerializable.createEnumCodec(AbilitySource::values, AbilitySource::valueOf);

    private final boolean placeOnBarWhenLearned;
    private final boolean useAbilityPool;
    private final boolean canUnlearn;

    AbilitySource(boolean placeOnBarWhenLearned, boolean useAbilityPool, boolean canUnlearn) {
        this.placeOnBarWhenLearned = placeOnBarWhenLearned;
        this.useAbilityPool = useAbilityPool;
        this.canUnlearn = canUnlearn;
    }

    public boolean placeOnBarWhenLearned() {
        return placeOnBarWhenLearned;
    }

    public boolean usesAbilityPool() {
        return useAbilityPool;
    }

    public boolean canUnlearn() {
        return canUnlearn;
    }

    @Nonnull
    @Override
    public String getString() {
        return name();
    }
}
