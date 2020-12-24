package com.chaosbuffalo.mkcore.core.damage;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKCombatFormulas;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Consumer;


public class MKDamageType extends ForgeRegistryEntry<MKDamageType> {
    private final RangedAttribute damageAttribute;
    private final RangedAttribute resistanceAttribute;
    private final RangedAttribute critAttribute;
    private final RangedAttribute critMultiplierAttribute;
    private final ResourceLocation iconLoc;
    private float critMultiplier;
    private boolean shouldDisplay;

    public MKDamageType(ResourceLocation name, RangedAttribute damageAttribute,
                        RangedAttribute resistanceAttribute, RangedAttribute critAttribute,
                        RangedAttribute critMultiplierAttribute) {
        setRegistryName(name);
        this.damageAttribute = damageAttribute;
        this.resistanceAttribute = resistanceAttribute;
        this.critMultiplierAttribute = critMultiplierAttribute;
        this.critAttribute = critAttribute;
        this.critMultiplier = 1.0f;
        this.shouldDisplay = true;
        iconLoc = new ResourceLocation(name.getNamespace(), String.format("textures/damage_types/%s.png",
                name.getPath().substring(7)));
    }

    public MKDamageType setCritMultiplier(float value) {
        this.critMultiplier = value;
        return this;
    }

    public MKDamageType setShouldDisplay(boolean shouldDisplay) {
        this.shouldDisplay = shouldDisplay;
        return this;
    }

    public boolean shouldDisplay() {
        return shouldDisplay;
    }

    public IFormattableTextComponent getDisplayName() {
        return new TranslationTextComponent(String.format("%s.%s.name", getRegistryName().getNamespace(),
                getRegistryName().getPath()));
    }

    public ResourceLocation getIcon() {
        return iconLoc;
    }

    public RangedAttribute getDamageAttribute() {
        return damageAttribute;
    }

    public RangedAttribute getCritChanceAttribute() {
        return critAttribute;
    }

    public RangedAttribute getCritMultiplierAttribute() {
        return critMultiplierAttribute;
    }

    public RangedAttribute getResistanceAttribute() {
        return resistanceAttribute;
    }

    public void registerAttributes(Consumer<Attribute> attributeMap) {
        attributeMap.accept(getDamageAttribute());
        attributeMap.accept(getResistanceAttribute());
    }

    public ITextComponent getEffectCritMessage(LivingEntity source, LivingEntity target, float damage,
                                               String damageType, boolean isSelf) {
        TranslationTextComponent msg;
        if (isSelf) {
            msg = new TranslationTextComponent("mkcore.crit.effect.self",
                    damageType,
                    target.getDisplayName(),
                    Math.round(damage));
        } else {
            msg = new TranslationTextComponent("mkcore.crit.effect.other",
                    source.getDisplayName(),
                    damageType,
                    target.getDisplayName(),
                    Math.round(damage));
        }
        return msg.mergeStyle(TextFormatting.DARK_PURPLE);
    }

    public ITextComponent getAbilityCritMessage(LivingEntity source, LivingEntity target, float damage,
                                                MKAbility ability, boolean isSelf) {
        TranslationTextComponent msg;
        if (isSelf) {
            msg = new TranslationTextComponent("mkcore.crit.ability.self",
                    ability.getAbilityName(),
                    target.getDisplayName(),
                    Math.round(damage));
        } else {
            msg = new TranslationTextComponent("mkcore.crit.ability.self",
                    source.getDisplayName(),
                    ability.getAbilityName(),
                    target.getDisplayName(),
                    Math.round(damage));
        }
        return msg.mergeStyle(TextFormatting.AQUA);
    }

    public float applyDamage(LivingEntity source, LivingEntity target, float originalDamage, float modifierScaling) {
        return applyDamage(source, target, source, originalDamage, modifierScaling);
    }

    public float applyDamage(LivingEntity source, LivingEntity target, Entity immediate, float originalDamage, float modifierScaling) {
        return (float) (originalDamage + source.getAttribute(getDamageAttribute()).getValue() * modifierScaling);
    }

    public float applyResistance(LivingEntity target, float originalDamage) {
        return (float) (originalDamage - (originalDamage * target.getAttribute(getResistanceAttribute()).getValue()));
    }

    public boolean rollCrit(LivingEntity source, LivingEntity target) {
        return rollCrit(source, target, source);
    }

    public boolean rollCrit(LivingEntity source, LivingEntity target, Entity immediate) {
        if (target.isEntityUndead()){
            return false;
        }
        float critChance = getCritChance(source, target, immediate);
        return MKCombatFormulas.checkCrit(source, critChance);
    }

    public float applyCritDamage(LivingEntity source, LivingEntity target, float originalDamage) {
        return applyCritDamage(source, target, source, originalDamage);
    }

    public float applyCritDamage(LivingEntity source, LivingEntity target, Entity immediate, float originalDamage) {
        return originalDamage * getCritMultiplier(source, target, immediate);
    }

    public float getCritMultiplier(LivingEntity source, LivingEntity target) {
        return getCritMultiplier(source, target, source);
    }

    public float getCritMultiplier(LivingEntity source, LivingEntity target, Entity immediate) {
        return (float) source.getAttribute(getCritMultiplierAttribute()).getValue();
    }

    public float getCritChance(LivingEntity source, LivingEntity target) {
        return getCritChance(source, target, source);
    }

    public float getCritChance(LivingEntity source, LivingEntity target, Entity immediate) {
        return (float) source.getAttribute(getCritChanceAttribute()).getValue() * critMultiplier;
    }
}
