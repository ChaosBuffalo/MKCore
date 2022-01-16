package com.chaosbuffalo.mkcore.core.healing;

import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class MKHealSource {

    @Nullable
    private final Entity directEntity;
    @Nullable
    private final LivingEntity sourceEntity;
    private final ResourceLocation abilityId;
    private boolean damagesUndead;
    private MKDamageType damageType;
    private float modifierScaling;

    public MKHealSource(ResourceLocation abilityId, @Nullable Entity directEntity, @Nullable LivingEntity sourceEntity,
                        MKDamageType damageType, float modifierScaling) {
        this.sourceEntity = sourceEntity;
        this.directEntity = directEntity;
        this.abilityId = abilityId;
        this.damagesUndead = true;
        this.damageType = damageType;
        this.modifierScaling = modifierScaling;
    }

    public static MKHealSource getHolyHeal(ResourceLocation abilityId, @Nullable Entity directEntity,
                                           @Nullable LivingEntity sourceEntity, float modifierScaling) {
        return new MKHealSource(abilityId, directEntity, sourceEntity, CoreDamageTypes.HolyDamage, modifierScaling);
    }

    public static MKHealSource getNatureHeal(ResourceLocation abilityId, @Nullable Entity directEntity,
                                             @Nullable LivingEntity sourceEntity, float modifierScaling) {
        return new MKHealSource(abilityId, directEntity, sourceEntity, CoreDamageTypes.NatureDamage, modifierScaling);
    }

    public MKDamageType getDamageType() {
        return damageType;
    }

    public float getModifierScaling() {
        return modifierScaling;
    }

    public MKHealSource setModifierScaling(float modifierScaling) {
        this.modifierScaling = modifierScaling;
        return this;
    }

    public MKHealSource setDamageType(MKDamageType damageType) {
        this.damageType = damageType;
        return this;
    }

    public MKHealSource setDamageUndead(boolean damagesUndead) {
        this.damagesUndead = damagesUndead;
        return this;
    }

    public boolean doesDamageUndead() {
        return damagesUndead;
    }

    @Nullable
    public Entity getDirectEntity() {
        return directEntity;
    }

    @Nullable
    public LivingEntity getSourceEntity() {
        return sourceEntity;
    }

    public ResourceLocation getAbilityId() {
        return abilityId;
    }
}
