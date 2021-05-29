package com.chaosbuffalo.mkcore.effects.status;

import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import com.chaosbuffalo.mkcore.effects.SpellPeriodicEffectBase;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectType;

public abstract class DamageTypeDotEffect extends SpellPeriodicEffectBase {
    public static final String SCALING_CONTRIBUTION = "dt_dot.scaling_contribution";
    private final MKDamageType damageType;
    private final String effectName;

    public DamageTypeDotEffect(MKDamageType damageType, int period, int liquidColor){
        super(period, EffectType.HARMFUL, liquidColor);
        this.damageType = damageType;
        this.effectName = String.format("%s.%s.dot", damageType.getRegistryName().getNamespace(),
                damageType.getRegistryName().getPath());
    }

    public static SpellCast Create(DamageTypeDotEffect effect, Entity source, float baseDamage, float scaling, float modifierScaling) {
        return effect.newSpellCast(source).setScalingParameters(baseDamage, scaling)
                .setFloat(SCALING_CONTRIBUTION, modifierScaling);
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }

    @Override
    public void doEffect(Entity applier, Entity caster, LivingEntity target, int i, SpellCast spellCast) {
        float damage = spellCast.getScaledValue(i);
        target.attackEntityFrom(MKDamageSource.causeEffectDamage(damageType, effectName,
                applier, caster,  spellCast.getFloat(SCALING_CONTRIBUTION)), damage);
    }

}
