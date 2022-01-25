package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.targeting_api.Targeting;
import com.chaosbuffalo.targeting_api.TargetingContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;

public abstract class WorldAreaEffectEntry {

    protected final TargetingContext targetContext;

    protected WorldAreaEffectEntry(TargetingContext context) {
        this.targetContext = context;
    }

    public abstract void apply(IMKEntityData casterData, IMKEntityData targetData);

    public static WorldAreaEffectEntry forEffect(Entity directSource, EffectInstance effect,
                                                 TargetingContext targetContext) {
        return new VanillaEffectEntry(directSource, effect, targetContext);
    }

    public static WorldAreaEffectEntry forEffect(Entity directSource, MKEffectBuilder<?> builder,
                                                 TargetingContext targetingContext) {
        return new MKEffectEntry(directSource, builder, targetingContext);
    }

    private static class MKEffectEntry extends WorldAreaEffectEntry {
        protected final MKEffectBuilder<?> newEffect;

        public MKEffectEntry(Entity directEntity, MKEffectBuilder<?> builder, TargetingContext targetingContext) {
            super(targetingContext);
            this.newEffect = builder.directEntity(directEntity);
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

    private static class VanillaEffectEntry extends WorldAreaEffectEntry {
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
