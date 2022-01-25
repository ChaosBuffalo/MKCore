package com.chaosbuffalo.mkcore.entities;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.targeting_api.Targeting;
import com.chaosbuffalo.targeting_api.TargetingContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;

public abstract class EffectEntry {

    protected final TargetingContext targetContext;

    private EffectEntry(TargetingContext context) {
        this.targetContext = context;
    }
    public abstract void apply(IMKEntityData casterData, IMKEntityData targetData);


    public static class MKEffectEntry extends EffectEntry {
        protected final MKEffectBuilder<?> newEffect;

        public MKEffectEntry(MKEffectBuilder<?> instance, TargetingContext targetingContext) {
            super(targetingContext);
            this.newEffect = instance;
        }

        @Override
        public void apply(IMKEntityData casterData, IMKEntityData targetData) {
            boolean validTarget = newEffect.getEffect().isValidTarget(targetContext, casterData, targetData);
            if (!validTarget) {
                return;
            }

            targetData.getEffects().addEffect(newEffect);
        }
    }

    public static class VanillaEffectEntry extends EffectEntry {
        protected final EffectInstance effect;
        protected final Entity directSource;

        VanillaEffectEntry(Entity directSource, EffectInstance effect, TargetingContext targetContext) {
            super(targetContext);
            this.directSource = directSource;
            this.effect = effect;
        }

        @Override
        public void apply(IMKEntityData casterData, IMKEntityData targetData) {
            LivingEntity target = targetData.getEntity();
            boolean validTarget = Targeting.isValidTarget(targetContext, casterData.getEntity(), target);

            if (!validTarget) {
                return;
            }

            if (effect.getPotion().isInstant()) {
                effect.getPotion().affectEntity(directSource, casterData.getEntity(), target, effect.getAmplifier(), 0.5D);
            } else {
                target.addPotionEffect(new EffectInstance(effect));
            }
        }
    }

}
