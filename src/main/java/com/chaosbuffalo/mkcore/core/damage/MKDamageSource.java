package com.chaosbuffalo.mkcore.core.damage;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class MKDamageSource extends DamageSource {
    protected final MKDamageType damageType;
    @Nullable
    protected Entity trueSource;
    @Nullable
    protected Entity immediateSource;
    protected float modifierScaling = 1.0f;
    protected boolean suppressTriggers;

    public enum Origination {
        MK_ABILITY,
        DAMAGE_TYPE
    }

    public abstract Origination getOrigination();

    @Override
    @Nullable
    public Entity getImmediateSource() {
        return immediateSource;
    }

    @Override
    @Nullable
    public Entity getTrueSource() {
        return trueSource;
    }

    private MKDamageSource(MKDamageType damageType,
                           @Nullable Entity immediateSource, @Nullable Entity trueSource) {
        super(damageType.getId().toString());
        this.immediateSource = immediateSource;
        this.trueSource = trueSource;
        this.damageType = damageType;
    }

    @Override
    public boolean isDifficultyScaled() {
        // We apply our own scaling
        return false;
    }

    public static class EffectDamage extends MKDamageSource {

        @Nullable
        protected final String damageTypeName;

        private EffectDamage(MKDamageType damageType, @Nullable Entity immediateSource, @Nullable Entity trueSource, @Nullable String damageTypeName) {
            super(damageType, immediateSource, trueSource);
            this.damageTypeName = damageTypeName;
        }

        @Nullable
        public String getDamageTypeName() {
            return damageTypeName;
        }

        @Override
        public Origination getOrigination() {
            return Origination.DAMAGE_TYPE;
        }

        @Nonnull
        @Override
        public ITextComponent getDeathMessage(LivingEntity killedEntity) {
            // FIXME: better message
            IFormattableTextComponent comp = new TranslationTextComponent("%s got dropped", killedEntity.getDisplayName());
            if (trueSource != null) {
                comp.appendString(" by ").appendSibling(trueSource.getDisplayName());
            } else {
                comp.appendString(" anonymously");
            }
            if (damageType != null || damageTypeName != null) {
                comp.appendString(" with some major ");
                if (damageTypeName != null) {
                    comp.appendSibling(new TranslationTextComponent(damageTypeName));
                } else {
                    comp.appendSibling(damageType.getDisplayName());
                }
            }
            return comp;
        }
    }

    public static class AbilityDamage extends MKDamageSource {
        @Nullable
        private final ResourceLocation abilityId;

        private AbilityDamage(MKDamageType damageType,
                              @Nullable Entity immediateSource,
                              @Nullable Entity trueSource,
                              @Nullable ResourceLocation abilityId) {
            super(damageType, immediateSource, trueSource);
            this.abilityId = abilityId;
        }

        @Nullable
        public ResourceLocation getAbilityId() {
            return abilityId;
        }

        @Override
        public Origination getOrigination() {
            return Origination.MK_ABILITY;
        }

        @Nonnull
        @Override
        public ITextComponent getDeathMessage(LivingEntity killedEntity) {
            // FIXME: better message
            IFormattableTextComponent comp = new TranslationTextComponent("%s got dropped", killedEntity.getDisplayName());
            if (trueSource != null) {
                comp.appendString(" by ").appendSibling(trueSource.getDisplayName());
            } else {
                comp.appendString(" anonymously");
            }
            if (abilityId != null) {
                MKAbility ability = MKCoreRegistry.getAbility(abilityId);
                if (ability != null) {
                    comp.appendString(" by ability ").appendSibling(ability.getAbilityName());
                }
            }
            if (damageType != null) {
                comp.appendString(" with some major ").appendSibling(damageType.getDisplayName());
            }
            return comp;
        }
    }

    public float getModifierScaling() {
        return modifierScaling;
    }

    public MKDamageSource setModifierScaling(float value) {
        modifierScaling = value;
        return this;
    }

    public MKDamageType getMKDamageType() {
        return damageType;
    }

    public boolean isMeleeDamage() {
        return damageType.equals(CoreDamageTypes.MeleeDamage);
    }

    public boolean shouldSuppressTriggers() {
        return suppressTriggers;
    }

    public MKDamageSource setSuppressTriggers(boolean suppressTriggers) {
        this.suppressTriggers = suppressTriggers;
        return this;
    }

    public static MKDamageSource causeAbilityDamage(MKDamageType damageType,
                                                    ResourceLocation abilityId,
                                                    @Nullable Entity immediateSource,
                                                    @Nullable Entity trueSource) {
        if (damageType.equals(CoreDamageTypes.MeleeDamage)) {
            return causeMeleeDamage(abilityId, immediateSource, trueSource);
        }
        return (MKDamageSource) new AbilityDamage(damageType, immediateSource, trueSource, abilityId)
                .setDamageBypassesArmor();
    }

    public static MKDamageSource causeAbilityDamage(MKDamageType damageType,
                                                    ResourceLocation abilityId,
                                                    @Nullable Entity immediateSource,
                                                    @Nullable Entity trueSource,
                                                    float modifierScaling) {
        return causeAbilityDamage(damageType, abilityId, immediateSource, trueSource)
                .setModifierScaling(modifierScaling);
    }

    public static MKDamageSource causeEffectDamage(MKDamageType damageType, String effectType,
                                                   @Nullable Entity immediateSource,
                                                   @Nullable Entity trueSource) {
        return (MKDamageSource) new EffectDamage(damageType, immediateSource, trueSource, effectType)
                .setDamageBypassesArmor();
    }

    public static MKDamageSource causeEffectDamage(MKDamageType damageType, String effectType,
                                                   @Nullable Entity immediateSource,
                                                   @Nullable Entity trueSource,
                                                   float modifierScaling) {
        return causeEffectDamage(damageType, effectType, immediateSource, trueSource)
                .setModifierScaling(modifierScaling);
    }


    public static MKDamageSource causeMeleeDamage(ResourceLocation abilityId,
                                                  @Nullable Entity immediateSource,
                                                  @Nullable Entity trueSource) {
        return new AbilityDamage(CoreDamageTypes.MeleeDamage, immediateSource, trueSource, abilityId);
    }

    public static MKDamageSource causeMeleeDamage(ResourceLocation abilityId,
                                                  @Nullable Entity immediateSource,
                                                  @Nullable Entity trueSource,
                                                  float modifierScaling) {
        return causeMeleeDamage(abilityId, immediateSource, trueSource)
                .setModifierScaling(modifierScaling);
    }
}
