package com.chaosbuffalo.mkcore.effects.status;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.instant.MKAbilityDamageEffect;
import net.minecraft.potion.EffectType;

public abstract class DamageTypeDotEffect extends MKEffect {

    public DamageTypeDotEffect() {
        super(EffectType.HARMFUL);
    }

    public static class State extends MKAbilityDamageEffect.State {
        private String effectName;

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect instance) {
            source = findEntity(source, instance.getSourceId(), targetData);

            float damage = getScaledValue(instance.getStackCount());
            if (effectName == null) {
                effectName = String.format("%s.%s.dot", damageType.getId().getNamespace(), damageType.getId().getPath());
            }

            targetData.getEntity().attackEntityFrom(
                    MKDamageSource.causeEffectDamage(damageType, effectName, source, source, getModifierScale()),
                    damage);
            return true;
        }
    }
}
