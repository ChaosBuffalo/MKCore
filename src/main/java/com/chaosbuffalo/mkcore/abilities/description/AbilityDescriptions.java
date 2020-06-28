package com.chaosbuffalo.mkcore.abilities.description;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.PassiveTalentAbility;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TranslationTextComponent;

public class AbilityDescriptions {

    public static AbilityDescription<MKAbility> getCooldownDescription(MKAbility ability){
        return new AbilityDescription<>(ability, (mkAbility, entityData) ->
                new TranslationTextComponent("mkcore.ability.description.cooldown",
                        String.format("%.1f seconds", (float) mkAbility.getCooldown() / GameConstants.TICKS_PER_SECOND)));
    }

    public static AbilityDescription<MKAbility> getCastTimeDescription(MKAbility ability){
        return new AbilityDescription<>(ability, (mkAbility, entityData) ->
                new TranslationTextComponent("mkcore.ability.description.cast_time",
                        mkAbility.getCastTime(entityData) > 0 ? String.format("%.1f seconds",
                                (float) mkAbility.getCastTime(entityData) / GameConstants.TICKS_PER_SECOND)
                                : I18n.format("mkcore.ability.description.instant")));
    }

    public static AbilityDescription<MKAbility> getManaCostDescription(MKAbility ability){
        return new AbilityDescription<>(ability, (mkAbility, entityData) ->
                new TranslationTextComponent("mkcore.ability.description.mana_cost",
                        Float.toString(mkAbility.getManaCost(entityData))));
    }

    public static AbilityDescription<PassiveTalentAbility> getPassiveTalentDescription(PassiveTalentAbility ability){
        return new AbilityDescription<>(ability, (mkAbility, entityData) ->
                new TranslationTextComponent("mkcore.ability.description.passive"));
    }

    public static AbilityDescription<MKAbility> getRangeDescription(MKAbility ability){
        return new AbilityDescription<>(ability, ((mkAbility, entityData) ->
                new TranslationTextComponent("mkcore.ability.description.range",
                        String.format("%.1f", ability.getDistance()))));
    }
}
