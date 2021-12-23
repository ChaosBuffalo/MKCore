package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.core.entity.EntityAbilityKnowledge;
import com.chaosbuffalo.mkcore.core.entity.EntityStatsModule;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;
import java.util.Objects;

public class MKEntityData implements IMKEntityData {

    private final LivingEntity entity;
    private final AbilityExecutor abilityExecutor;
    private final EntityStatsModule stats;
    private final EntityAbilityKnowledge knowledge;
    private final CombatExtensionModule combatExtensionModule;

    public MKEntityData(LivingEntity livingEntity) {
        entity = Objects.requireNonNull(livingEntity);
        knowledge = new EntityAbilityKnowledge(this);
        abilityExecutor = new AbilityExecutor(this);
        stats = new EntityStatsModule(this);
        combatExtensionModule = new CombatExtensionModule(this);
    }

    public void update() {
        getEntity().getEntityWorld().getProfiler().startSection("MKEntityData.update");

        getEntity().getEntityWorld().getProfiler().startSection("AbilityExecutor.tick");
        getAbilityExecutor().tick();
        getEntity().getEntityWorld().getProfiler().endStartSection("EntityStats.tick");
        getStats().tick();
        getEntity().getEntityWorld().getProfiler().endStartSection("EntityCombat.tick");
        getCombatExtension().tick();
        getEntity().getEntityWorld().getProfiler().endSection();

        getEntity().getEntityWorld().getProfiler().endSection();
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
    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.put("knowledge", getKnowledge().serialize());
        return tag;
    }

    @Override
    public void deserialize(CompoundNBT tag) {
        getKnowledge().deserialize(tag.getCompound("knowledge"));
    }
}
