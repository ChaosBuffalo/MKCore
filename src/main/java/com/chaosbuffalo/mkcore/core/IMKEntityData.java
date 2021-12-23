package com.chaosbuffalo.mkcore.core;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;

public interface IMKEntityData {

    @Nonnull
    LivingEntity getEntity();

    AbilityExecutor getAbilityExecutor();

    IMKEntityKnowledge getKnowledge();

    IMKEntityStats getStats();

    CombatExtensionModule getCombatExtension();

    CompoundNBT serialize();

    void deserialize(CompoundNBT nbt);

}
