package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;


public class MKAbilityInfo implements IMKSerializable<CompoundNBT> {
    private final MKAbility ability;
    private boolean known;
    private AbilitySource source = AbilitySource.TRAINED;

    public MKAbilityInfo(MKAbility ability, AbilitySource source) {
        this.ability = ability;
        known = false;
        this.source = source;
    }

    @Nonnull
    public MKAbility getAbility() {
        return ability;
    }

    public ResourceLocation getId() {
        return ability.getAbilityId();
    }

    public boolean isCurrentlyKnown() {
        return known;
    }

    public void setKnown(boolean learn) {
        this.known = learn;
    }

    public void setSource(AbilitySource source) {
        this.source = source;
    }

    public AbilitySource getSource() {
        return source;
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.putBoolean("known", known);
        tag.putString("source", source.name());
        return tag;
    }

    @Override
    public boolean deserialize(CompoundNBT tag) {
        if (tag.contains("known")) {
            known = tag.getBoolean("known");
        }
        if (tag.contains("source")) {
            source = AbilitySource.valueOf(tag.getString("source"));
        }
        return true;
    }
}
