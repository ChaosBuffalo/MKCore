package com.chaosbuffalo.mkcore.core.entity;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.*;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public class EntityStatsModule implements IMKEntityStats {

    protected final IMKEntityData entityData;
    protected final AbilityTracker abilityTracker;

    public EntityStatsModule(IMKEntityData data) {
        entityData = data;
        abilityTracker = AbilityTracker.getTracker(data.getEntity());
    }

    @Override
    public float getDamageTypeBonus(MKDamageType damageType) {
        return (float) getEntity().getAttribute(damageType.getDamageAttribute()).getValue();
    }

    public void tick() {
        abilityTracker.tick();
    }

    @Override
    public float getHealBonus() {
        return (float) getEntity().getAttribute(MKAttributes.HEAL_BONUS).getValue();
    }

    @Override
    public float getBuffDurationModifier() {
        return (float) getEntity().getAttribute(MKAttributes.BUFF_DURATION).getValue();
    }

    @Override
    public float getHealth() {
        return getEntity().getHealth();
    }

    @Override
    public void setHealth(float value) {
        getEntity().setHealth(value);
    }

    @Override
    public float getMaxHealth() {
        return getEntity().getMaxHealth();
    }

    @Override
    public int getAbilityCooldown(MKAbility ability) {
        int ticks = ability.getCooldown(entityData);
        return MKCombatFormulas.applyCooldownReduction(entityData, ticks);
    }

    @Override
    public int getAbilityCastTime(MKAbility ability) {
        int ticks = ability.getCastTime(entityData);
        return ability.canApplyCastingSpeedModifier() ?
                MKCombatFormulas.applyCastTimeModifier(entityData, ticks) :
                ticks;
    }

    @Override
    public boolean canActivateAbility(MKAbility ability) {
        return true;
    }

    @Override
    public void setTimer(ResourceLocation id, int cooldown) {
        if (cooldown > 0) {
            abilityTracker.setCooldown(id, cooldown);
        } else {
            abilityTracker.removeCooldown(id);
        }
    }

    @Override
    public int getTimer(ResourceLocation id) {
        return abilityTracker.getCooldownTicks(id);
    }

    @Override
    public float getTimerPercent(ResourceLocation timerId, float partialTick) {
        return abilityTracker.getCooldownPercent(timerId, partialTick);
    }

    @Override
    public void resetAllTimers() {
        abilityTracker.removeAll();
    }

    @Override
    public CompoundNBT serialize() {
        return new CompoundNBT();
    }

    @Override
    public void deserialize(CompoundNBT nbt) {

    }

    public LivingEntity getEntity() {
        return entityData.getEntity();
    }

}
