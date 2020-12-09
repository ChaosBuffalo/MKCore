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
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.function.Consumer;

public class MeleeDamageType extends MKDamageType {

    public MeleeDamageType(ResourceLocation name) {
        super(name, (RangedAttribute) Attributes.ATTACK_DAMAGE,
                (RangedAttribute) Attributes.ARMOR_TOUGHNESS, MKAttributes.MELEE_CRIT,
                MKAttributes.MELEE_CRIT_MULTIPLIER);
    }

    @Override
    public ITextComponent getAbilityCritMessage(LivingEntity source, LivingEntity target, float damage,
                                                MKAbility ability, boolean isSelf) {
        String msg;
        if (isSelf) {
            msg = String.format("You just crit %s with %s for %s",
                    target.getDisplayName().getString(),
                    source.getHeldItemMainhand().getDisplayName().getString(),
                    Math.round(damage));
        } else {
            msg = String.format("%s just crit %s with %s for %s",
                    source.getDisplayName().getString(),
                    target.getDisplayName().getString(),
                    source.getHeldItemMainhand().getDisplayName().getString(),
                    Math.round(damage));
        }
        return new StringTextComponent(msg).mergeStyle(TextFormatting.GOLD);
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
