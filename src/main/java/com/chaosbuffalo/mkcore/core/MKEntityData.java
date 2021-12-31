package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.core.entity.EntityAbilityKnowledge;
import com.chaosbuffalo.mkcore.core.entity.EntityEffectHandler;
import com.chaosbuffalo.mkcore.core.entity.EntityStatsModule;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;
import java.util.Objects;

public class MKEntityData implements IMKEntityData {

    private final LivingEntity entity;
    private final AbilityExecutor abilityExecutor;
    private final EntityStatsModule stats;
    private final EntityAbilityKnowledge knowledge;
    private final CombatExtensionModule combatExtensionModule;
    private final EntityEffectHandler effectHandler;

    public MKEntityData(LivingEntity livingEntity) {
        entity = Objects.requireNonNull(livingEntity);
        knowledge = new EntityAbilityKnowledge(this);
        abilityExecutor = new AbilityExecutor(this);
        stats = new EntityStatsModule(this);
        combatExtensionModule = new CombatExtensionModule(this);
        effectHandler = new EntityEffectHandler(this);
    }

    @Nonnull
    @Override
    public LivingEntity getEntity() {
        return entity;
    }

    @Override
    public AbilityExecutor getAbilityExecutor() {
        return abilityExecutor;
    }

    @Override
    public EntityAbilityKnowledge getKnowledge() {
        return knowledge;
    }

    @Override
    public EntityStatsModule getStats() {
        return stats;
    }

    @Override
    public CombatExtensionModule getCombatExtension() {
        return combatExtensionModule;
    }

    @Override
    public EntityEffectHandler getEffects() {
        return effectHandler;
    }

    @Override
    public void onJoinWorld() {
        getEffects().onJoinWorld();
    }

    public void update() {
        getEntity().getEntityWorld().getProfiler().startSection("MKEntityData.update");

        getEntity().getEntityWorld().getProfiler().startSection("EntityEffects.tick");
        getEffects().tick();
        getEntity().getEntityWorld().getProfiler().endStartSection("AbilityExecutor.tick");
        getAbilityExecutor().tick();
        getEntity().getEntityWorld().getProfiler().endStartSection("EntityStats.tick");
        getStats().tick();
        getEntity().getEntityWorld().getProfiler().endStartSection("EntityCombat.tick");
        getCombatExtension().tick();
        getEntity().getEntityWorld().getProfiler().endSection();

        getEntity().getEntityWorld().getProfiler().endSection();
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.put("knowledge", getKnowledge().serialize());
        tag.put("effects", effectHandler.serialize());
        return tag;
    }

    @Override
    public void deserialize(CompoundNBT tag) {
        getKnowledge().deserialize(tag.getCompound("knowledge"));
        effectHandler.deserialize(tag.getCompound("effects"));
    }

    @Override
    public void onPlayerStartTracking(ServerPlayerEntity playerEntity) {
        getEffects().sendAllEffectsToPlayer(playerEntity);
    }
}
