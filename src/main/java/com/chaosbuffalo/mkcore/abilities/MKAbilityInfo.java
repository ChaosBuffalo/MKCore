package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Arrays;


public class MKAbilityInfo implements IMKSerializable<CompoundNBT> {
    private final MKAbility ability;
    private AbilitySource source;
    private int sourceMask;

    public MKAbilityInfo(MKAbility ability, AbilitySource source) {
        this.ability = ability;
        addSource(source);
    }

    @Nonnull
    public MKAbility getAbility() {
        return ability;
    }

    public ResourceLocation getId() {
        return ability.getAbilityId();
    }

    public boolean isCurrentlyKnown() {
        return sourceMask != 0;
    }

    public boolean hasSource(AbilitySource source) {
        return (sourceMask & source.mask()) != 0;
    }

    private void setSources(int mask) {
        sourceMask = mask;
        updateHighestSource();
    }

    public void addSource(AbilitySource source) {
        sourceMask |= source.mask();
        updateHighestSource();
    }

    public void removeSource(AbilitySource source) {
        sourceMask &= ~source.mask();
        updateHighestSource();
    }

    private void updateHighestSource() {
        for (AbilitySource src : AbilitySource.VALUES) {
            if (hasSource(src)) {
                source = src;
            }
        }
    }

    public AbilitySource getSource() {
        return source;
    }

    public boolean canUnlearnByCommand() {
        return Arrays.stream(AbilitySource.VALUES)
                .filter(this::hasSource)
                .anyMatch(AbilitySource::isSimple);
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("mask", sourceMask);
        return tag;
    }

    @Override
    public boolean deserialize(CompoundNBT tag) {
        if (tag.contains("mask")) {
            setSources(tag.getInt("mask"));
        }
        return true;
    }

    @Override
    public String toString() {
        return "MKAbilityInfo{" +
                "ability=" + ability +
                ", mask=" + sourceMask +
                ", source=" + source +
                '}';
    }
}
