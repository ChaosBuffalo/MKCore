package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.core.entity.EntityEffectHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;

public interface IMKEntityData {

    @Nonnull
    LivingEntity getEntity();

    default boolean isServerSide() {
        return !getEntity().getEntityWorld().isRemote();
    }

    AbilityExecutor getAbilityExecutor();

    IMKEntityKnowledge getKnowledge();

    IMKEntityStats getStats();

    CombatExtensionModule getCombatExtension();

    EntityEffectHandler getEffects();

    CompoundNBT serialize();

    void deserialize(CompoundNBT nbt);

    void onJoinWorld();

    void onPlayerStartTracking(ServerPlayerEntity playerEntity);
}
