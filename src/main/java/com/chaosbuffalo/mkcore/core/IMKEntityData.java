package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.core.entity.EntityEntitlementsKnowledge;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;

public interface IMKEntityData {

    LivingEntity getEntity();

    AbilityExecutor getAbilityExecutor();

    IMKEntityKnowledge getKnowledge();

    IMKEntityStats getStats();

    CombatExtensionModule getCombatExtension();

    CompoundNBT serialize();

    void deserialize(CompoundNBT nbt);

}
