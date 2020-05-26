package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.utils.EntityUtils;
import com.chaosbuffalo.mkcore.utils.ItemUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;

public class PlayerFormulas {

    public static int applyCooldownReduction(IMKPlayerData playerData, int originalCooldownTicks) {
        final float MAX_COOLDOWN = 2.0f; // Maximum cooldown rate improvement is 200%
        float cdrValue = (float) playerData.getPlayer().getAttribute(PlayerAttributes.COOLDOWN).getValue();
        float mod = MAX_COOLDOWN - cdrValue;
        float newTicks = mod * originalCooldownTicks;
        return (int) newTicks;
    }

    public static float scaleMagicDamage(IMKPlayerData playerData, float originalDamage, float modifierScaling) {
        float mod = playerData.getStats().getMagicDamageBonus();
        return originalDamage + mod * modifierScaling;
    }

    public static float applyMagicArmor(IMKPlayerData playerData, float originalDamage) {
        float mod = playerData.getStats().getMagicArmor();
        return originalDamage - mod;
    }


    public static float applyManaCostReduction(IMKPlayerData playerData, float originalCost) {
        return originalCost;
    }

//    public static float applyHealBonus(IMKPlayerData playerData, float amount) {
//        float mod = playerData.getHealBonus();
//        return amount * mod;
//    }

    public static float getMeleeCritChanceForItem(IMKPlayerData data, ServerPlayerEntity player, ItemStack item) {
        return data.getStats().getMeleeCritChance() + ItemUtils.getCritChanceForItem(item);
    }

    public static float getRangedCritChanceForEntity(IMKPlayerData data, ServerPlayerEntity player, Entity entity) {
        return EntityUtils.ENTITY_CRIT.getChance(entity);
    }

//    public static int applyBuffDurationBonus(IPlayerData data, int duration) {
//        return (int) (duration * data.getBuffDurationBonus());
//    }
}
