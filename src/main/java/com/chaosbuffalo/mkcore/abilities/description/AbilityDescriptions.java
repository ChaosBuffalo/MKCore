package com.chaosbuffalo.mkcore.abilities.description;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
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
import java.util.function.Function;

public class AbilityDescriptions {

    public static ITextComponent getCooldownDescription(MKAbility ability, IMKEntityData entityData) {
        float seconds = (float) entityData.getStats().getAbilityCooldown(ability) / GameConstants.TICKS_PER_SECOND;
        return new TranslationTextComponent("mkcore.ability.description.cooldown", seconds);
    }

    public static ITextComponent getCastTimeDescription(MKAbility ability, IMKEntityData entityData) {
        int castTicks = entityData.getStats().getAbilityCastTime(ability);
        float seconds = (float) castTicks / GameConstants.TICKS_PER_SECOND;
        ITextComponent time = castTicks > 0 ?
                new TranslationTextComponent("mkcore.ability.description.seconds", seconds) :
                new TranslationTextComponent("mkcore.ability.description.instant");
        return new TranslationTextComponent("mkcore.ability.description.cast_time", time);
    }

    public static ITextComponent getManaCostDescription(MKAbility ability, IMKEntityData entityData) {
        return new TranslationTextComponent("mkcore.ability.description.mana_cost", ability.getManaCost(entityData));
    }

    public static ITextComponent getRangeDescription(MKAbility ability, IMKEntityData entityData) {
        return new TranslationTextComponent("mkcore.ability.description.range", ability.getDistance(entityData.getEntity()));
    }

    public static ITextComponent getAbilityDescription(MKAbility ability, IMKEntityData entityData,
                                                       Function<IMKEntityData, List<Object>> argsProvider) {
        return new TranslationTextComponent(ability.getDescriptionTranslationKey(), argsProvider.apply(entityData).toArray());
    }

    public static List<ITextComponent> getEffectModifiers(Effect effect, IMKEntityData entityData, boolean showName) {
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
                    .append(new TranslationTextComponent(entry.getKey().getAttributeName()))
                    .appendString(String.format(": %s%.2f", entry.getValue().getAmount() > 0 ? "+" : "", entry.getValue().getAmount())));
        }
        return desc;
    }
}
