package com.chaosbuffalo.mkcore.core;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;

public interface IMKEntityData {

    LivingEntity getEntity();

    AbilityExecutor getAbilityExecutor();

    IMKEntityKnowledge getKnowledge();

    IStatsModule getStats();

    CombatExtensionModule getCombatExtension();

    CompoundNBT serialize();

    void deserialize(CompoundNBT nbt);

}
