package com.chaosbuffalo.mkcore.core.damage;

import com.chaosbuffalo.mkcore.init.ModDamageTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class MKDamageSource extends IndirectEntityDamageSource {
    @Nullable
    private ResourceLocation abilityId;
    @Nullable
    private String damageTypeName;
    private float modifierScaling;
    private boolean suppressTriggers;
    private final MKDamageType damageType;

    public enum Origination {
        MK_ABILITY,
        DAMAGE_TYPE
    }

    private final Origination origination;

    public ResourceLocation getAbilityId() {
        return abilityId;
    }

    public Origination getOrigination() {
        return origination;
    }

    @Nullable
    public String getDamageTypeName() {
        return damageTypeName;
    }

    public MKDamageSource(String damageTypeName, MKDamageType damageTypeIn,
                          Entity source, @Nullable Entity indirectEntityIn) {
        super(damageTypeIn.getRegistryName().toString(), source, indirectEntityIn);
        this.damageType = damageTypeIn;
        this.damageTypeName = damageTypeName;
        this.origination = Origination.DAMAGE_TYPE;

    }

    public MKDamageSource(ResourceLocation abilityId, MKDamageType damageTypeIn,
                          Entity source, @Nullable Entity indirectEntityIn) {
        super(damageTypeIn.getRegistryName().toString(), source, indirectEntityIn);
        this.abilityId = abilityId;
        this.modifierScaling = 1.0f;
        this.damageType = damageTypeIn;
        this.origination = Origination.MK_ABILITY;
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
        return damageType.equals(ModDamageTypes.MeleeDamage);
    }

    public boolean shouldSuppressTriggers() {
        return suppressTriggers;
    }

    public MKDamageSource setSuppressTriggers(boolean suppressTriggers) {
        this.suppressTriggers = suppressTriggers;
        return this;
    }

    public static MKDamageSource causeAbilityDamage(MKDamageType damageType, ResourceLocation abilityId, Entity source,
                                                    @Nullable Entity indirectEntityIn) {
        if (damageType.equals(ModDamageTypes.MeleeDamage)) {
            return causeMeleeDamage(abilityId, source, indirectEntityIn);
        }
        return (MKDamageSource) new MKDamageSource(abilityId, damageType, source, indirectEntityIn)
                .setDamageBypassesArmor();
    }

    public static MKDamageSource causeAbilityDamage(MKDamageType damageType, ResourceLocation abilityId, Entity source,
                                                    @Nullable Entity indirectEntityIn, float modifierScaling) {
        return causeAbilityDamage(damageType, abilityId, source, indirectEntityIn)
                .setModifierScaling(modifierScaling);
    }

    public static MKDamageSource causeEffectDamage(MKDamageType damageType, String effectType, Entity source,
                                                   @Nullable Entity indirectEntityIn) {
        return (MKDamageSource) new MKDamageSource(effectType, damageType, source, indirectEntityIn)
                .setDamageBypassesArmor();
    }

    public static MKDamageSource causeEffectDamage(MKDamageType damageType, String effectType, Entity source,
                                                   @Nullable Entity indirectEntityIn, float modifierScaling) {
        return causeEffectDamage(damageType, effectType, source, indirectEntityIn)
                .setModifierScaling(modifierScaling);
    }


    public static MKDamageSource causeMeleeDamage(ResourceLocation abilityId, Entity source,
                                                  @Nullable Entity indirectEntityIn) {
        return new MKDamageSource(abilityId, ModDamageTypes.MeleeDamage, source, indirectEntityIn);
    }

    public static MKDamageSource causeMeleeDamage(ResourceLocation abilityId, Entity source,
                                                  @Nullable Entity indirectEntityIn, float modifierScaling) {
        return causeMeleeDamage(abilityId, source, indirectEntityIn).setModifierScaling(modifierScaling);
    }
}
