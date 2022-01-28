package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public interface IMKEntityStats {

    float getDamageTypeBonus(MKDamageType damageType);

    float getHealBonus();

    float getBuffDurationModifier();

    float getHealth();

    void setHealth(float value);

    float getMaxHealth();

    int getAbilityCooldown(MKAbility ability);

    int getAbilityCastTime(MKAbility ability);

    boolean canActivateAbility(MKAbility ability);

    void setTimer(ResourceLocation id, int cooldown);

    void setLocalTimer(ResourceLocation id, int cooldown);

    int getTimer(ResourceLocation id);

    float getTimerPercent(ResourceLocation id, float partialTick);

    void resetAllTimers();

    CompoundNBT serialize();

    void deserialize(CompoundNBT nbt);
}
