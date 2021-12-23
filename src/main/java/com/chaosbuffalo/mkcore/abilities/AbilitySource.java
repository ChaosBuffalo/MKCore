package com.chaosbuffalo.mkcore.abilities;

import com.mojang.serialization.Codec;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;
import java.util.EnumSet;

public enum AbilitySource implements IStringSerializable {
    ITEM(1, SourceFlags.HasComplexAcquisition),
    TRAINED(2, SourceFlags.PlaceOnBarWhenLearned, SourceFlags.UseAbilityPool),
    GRANTED(4, SourceFlags.PlaceOnBarWhenLearned),
    // Talents are stored separately and this source is granted to the entity upon talent record deserialization.
    // This is mostly to support the case where the talent tree version changes and no longer provides an ability it used to.
    // In that case there would be no way to know that the ability should be forgotten by the player
    TALENT(8, SourceFlags.HasComplexAcquisition),
    ADMIN(16);

    public static final AbilitySource[] VALUES = values();
    public static final Codec<AbilitySource> CODEC = IStringSerializable.createEnumCodec(AbilitySource::values, AbilitySource::valueOf);

    private enum SourceFlags {
        PlaceOnBarWhenLearned(1),
        UseAbilityPool(2),
        HasComplexAcquisition(4);

        private final int flag;

        SourceFlags(int v) {
            this.flag = v;
        }

        public int flag() {
            return flag;
        }
    }

    private final int mask;
    private final EnumSet<SourceFlags> flags;

    AbilitySource(int mask, SourceFlags... options) {
        this.mask = mask;
        this.flags = options.length > 0 ?
                EnumSet.of(options[0], options) :
                EnumSet.noneOf(SourceFlags.class);
    }

    public boolean placeOnBarWhenLearned() {
        return flags.contains(SourceFlags.PlaceOnBarWhenLearned);
    }

    public boolean usesAbilityPool() {
        return flags.contains(SourceFlags.UseAbilityPool);
    }

    // Whether the Ability can be forgotten without prerequisites, such as being granted by a talent or item
    public boolean isSimple() {
        return !flags.contains(SourceFlags.HasComplexAcquisition);
    }

    public int mask() {
        return mask;
    }

    @Nonnull
    @Override
    public String getString() {
        return name();
    }
}
