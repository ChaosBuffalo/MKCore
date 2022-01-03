package com.chaosbuffalo.mkcore.abilities.description;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.potion.Effect;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AbilityDescriptions {

    public static ITextComponent getRangeDescription(MKAbility ability, IMKEntityData casterData) {
        return new TranslationTextComponent("mkcore.ability.description.range", ability.getDistance(casterData.getEntity()));
    }

    public static List<ITextComponent> getEffectModifiers(Effect effect, IMKEntityData casterData, boolean showName) {
        if (effect.getAttributeModifierMap().isEmpty()) {
            return Collections.emptyList();
        }
        List<ITextComponent> desc = new ArrayList<>(4);
        if (showName) {
            desc.add(new TranslationTextComponent("mkcore.ability.description.effect_with_name", effect.getDisplayName()));
        } else {
            desc.add(new TranslationTextComponent("mkcore.ability.description.effect"));
        }
        for (Map.Entry<Attribute, AttributeModifier> entry : effect.getAttributeModifierMap().entrySet()) {
            desc.add(new StringTextComponent("    ")
                    .appendSibling(new TranslationTextComponent(entry.getKey().getAttributeName()))
                    .appendString(String.format(": %s%.2f ", entry.getValue().getAmount() > 0 ? "+" : "", entry.getValue().getAmount()))
                    .appendSibling(new TranslationTextComponent("mkcore.ability.description.per_level")));
        }
        return desc;
    }

    public static List<ITextComponent> getEffectModifiers(MKEffect effect, IMKEntityData casterData, boolean showName) {
        if (effect.getAttributeModifierMap().isEmpty()) {
            return Collections.emptyList();
        }
        List<ITextComponent> desc = new ArrayList<>(4);
        if (showName) {
            desc.add(new TranslationTextComponent("mkcore.ability.description.effect_with_name", effect.getDisplayName()));
        } else {
            desc.add(new TranslationTextComponent("mkcore.ability.description.effect"));
        }
        for (Map.Entry<Attribute, AttributeModifier> entry : effect.getAttributeModifierMap().entrySet()) {
            desc.add(new StringTextComponent("    ")
                    .appendSibling(new TranslationTextComponent(entry.getKey().getAttributeName()))
                    .appendString(String.format(": %s%.2f ", entry.getValue().getAmount() > 0 ? "+" : "", entry.getValue().getAmount()))
                    .appendSibling(new TranslationTextComponent("mkcore.ability.description.per_level")));
        }
        return desc;
    }
}
