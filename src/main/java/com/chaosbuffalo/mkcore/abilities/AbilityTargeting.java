package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.description.AbilityDescriptions;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.LivingEntity;

public class AbilityTargeting {

    public static final AbilityTargetSelector NONE = new AbilityTargetSelector((entityData, ability) -> AbilityContext.EMPTY)
            .setDescriptionKey("mkcore.ability_target.none");

    public static final AbilityTargetSelector PROJECTILE = new AbilityTargetSelector((entityData, ability) -> AbilityContext.EMPTY)
            .setDescriptionKey("mkcore.ability_target.projectile");

    public static final AbilityTargetSelector LINE = new AbilityTargetSelector((entityData, ability) -> AbilityContext.EMPTY)
            .addDynamicDescription(AbilityDescriptions::getRangeDescription)
            .setDescriptionKey("mkcore.ability_target.line");

    public static final AbilityTargetSelector SELF = new AbilityTargetSelector(AbilityTargeting::selectSelf)
            .setRequiredMemories(ImmutableSet.of(MKAbilityMemories.ABILITY_TARGET))
            .setShowTargetType(false)
            .setDescriptionKey("mkcore.ability_target.self");

    public static final AbilityTargetSelector SINGLE_TARGET = new AbilityTargetSelector(AbilityTargeting::selectSingle)
            .setRequiredMemories(ImmutableSet.of(MKAbilityMemories.ABILITY_TARGET))
            .addDynamicDescription(AbilityDescriptions::getRangeDescription)
            .setDescriptionKey("mkcore.ability_target.single_target");

    public static final AbilityTargetSelector SINGLE_TARGET_OR_SELF = new AbilityTargetSelector(AbilityTargeting::selectSingleOrSelf)
            .setRequiredMemories(ImmutableSet.of(MKAbilityMemories.ABILITY_TARGET))
            .addDynamicDescription(AbilityDescriptions::getRangeDescription)
            .setDescriptionKey("mkcore.ability_target.single_target_self");

    public static final AbilityTargetSelector PBAOE = new AbilityTargetSelector((entityData, mkAbility) -> AbilityContext.EMPTY)
            .setDescriptionKey("mkcore.ability_target.pbaoe")
            .addDynamicDescription(AbilityDescriptions::getRangeDescription);


    private static AbilityContext selectSelf(IMKEntityData entityData, MKAbility ability) {
        MKCore.LOGGER.info("AbilityTargeting.SELF {} {}", ability.getAbilityId(), entityData.getEntity());
        return AbilityContext.selfTarget(entityData);
    }

    private static AbilityContext selectSingle(IMKEntityData entityData, MKAbility ability) {
        LivingEntity targetEntity = ability.getSingleLivingTarget(entityData.getEntity(),
                ability.getDistance(entityData.getEntity()));
        MKCore.LOGGER.info("AbilityTargeting.SINGLE_TARGET {} {} {}", ability.getAbilityId(), entityData.getEntity(), targetEntity);
        return AbilityContext.singleTarget(targetEntity);
    }

    private static AbilityContext selectSingleOrSelf(IMKEntityData entityData, MKAbility ability) {
        LivingEntity targetEntity = ability.getSingleLivingTargetOrSelf(entityData.getEntity(),
                ability.getDistance(entityData.getEntity()), true);
        MKCore.LOGGER.info("AbilityTargeting.SINGLE_TARGET_OR_SELF {} {} {}", ability.getAbilityId(),
                entityData.getEntity(), targetEntity);
        return AbilityContext.singleTarget(targetEntity);
    }
}
