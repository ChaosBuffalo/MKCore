package com.chaosbuffalo.mkcore.core.damage;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.util.CombatRules;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Consumer;

public class MeleeDamageType extends MKDamageType {

    public MeleeDamageType(ResourceLocation name) {
        super(name, (RangedAttribute) Attributes.ATTACK_DAMAGE,
                (RangedAttribute) Attributes.ARMOR_TOUGHNESS, MKAttributes.MELEE_CRIT,
                MKAttributes.MELEE_CRIT_MULTIPLIER, TextFormatting.DARK_GRAY);
    }

    @Override
    public ITextComponent getAbilityCritMessage(LivingEntity source, LivingEntity target, float damage,
                                                MKAbility ability, boolean isSelf) {
        TranslationTextComponent msg;
        if (isSelf) {
            msg = new TranslationTextComponent("mkcore.crit.melee.self",
                    target.getDisplayName(),
                    source.getHeldItemMainhand().getDisplayName(),
                    Math.round(damage));
        } else {
            msg = new TranslationTextComponent("mkcore.crit.melee.other",
                    source.getDisplayName(),
                    target.getDisplayName(),
                    source.getHeldItemMainhand().getDisplayName(),
                    Math.round(damage));
        }
        return msg.mergeStyle(TextFormatting.GOLD);
    }

    @Override
    public void registerAttributes(Consumer<Attribute> attributeMap) {

    }


    @Override
    public float applyResistance(LivingEntity target, float originalDamage) {
        return CombatRules.getDamageAfterAbsorb(originalDamage, target.getTotalArmorValue(),
                (float) target.getAttribute(getResistanceAttribute()).getValue());
    }
}
