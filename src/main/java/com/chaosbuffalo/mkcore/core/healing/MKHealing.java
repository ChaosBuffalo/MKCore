package com.chaosbuffalo.mkcore.core.healing;

import com.chaosbuffalo.mkcore.MKConfig;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKCombatFormulas;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;

public class MKHealing {

    public static void healEntityFrom(LivingEntity target, float amount, MKHealSource healSource) {
        float finalValue = MKCore.getEntityData(healSource.getTrueSource())
                .map(data -> MKCombatFormulas.applyHealBonus(data, amount, healSource.getModifierScaling()))
                .orElse(amount);
        MKAbilityHealEvent event = new MKAbilityHealEvent(target, finalValue, healSource);
        if (!MinecraftForge.EVENT_BUS.post(event)) {
            if (MKConfig.SERVER.healsDamageUndead.get() && healSource.doesDamageUndead()
                    && isEnemyUndead(healSource.getTrueSource(), target)) {
                float healDamageMultiplier = MKConfig.SERVER.undeadHealDamageMultiplier.get().floatValue();
                target.attackEntityFrom(MKDamageSource.causeAbilityDamage(healSource.getDamageType(),
                        healSource.getAbilityId(), healSource.getImmediateSource(), healSource.getTrueSource()),
                        healDamageMultiplier * event.getAmount());
            } else {
                target.heal(event.getAmount());
            }
        }
    }


    public static boolean isEnemyUndead(@Nullable Entity source, LivingEntity target){
        return (target.isEntityUndead() && (source == null || Targeting.isValidEnemy(source, target)));
    }
}
