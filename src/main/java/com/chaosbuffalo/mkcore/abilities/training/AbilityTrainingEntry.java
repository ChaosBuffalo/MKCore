package com.chaosbuffalo.mkcore.abilities.training;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKPlayerData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AbilityTrainingEntry {

    private final MKAbility ability;
    private final boolean isPoolAbility;
    private final List<IAbilityTrainingRequirement> requirementList;

    public AbilityTrainingEntry(MKAbility ability, boolean useAbilityPool) {
        this.ability = ability;
        requirementList = new ArrayList<>();
        isPoolAbility = useAbilityPool;
    }

    public MKAbility getAbility() {
        return ability;
    }

    public List<IAbilityTrainingRequirement> getRequirements() {
        return requirementList;
    }

    public boolean isPoolAbility() {
        return isPoolAbility;
    }

    public AbilityTrainingEntry addRequirement(IAbilityTrainingRequirement requirement) {
        requirementList.add(requirement);
        return this;
    }

    public boolean checkRequirements(MKPlayerData playerData) {
        return getRequirements().stream().allMatch(req -> req.check(playerData, ability));
    }

    public void onAbilityLearned(MKPlayerData playerData) {
        getRequirements().forEach(req -> req.onLearned(playerData, ability));
    }

    private AbilityRequirementEvaluation evaluateRequirement(IAbilityTrainingRequirement req, MKPlayerData playerData) {
        return new AbilityRequirementEvaluation(req.describe(), req.check(playerData, getAbility()));
    }

    public AbilityTrainingEvaluation evaluate(MKPlayerData playerData) {
        List<AbilityRequirementEvaluation> requirements = getRequirements()
                .stream()
                .map(req -> evaluateRequirement(req, playerData))
                .collect(Collectors.toList());
        return new AbilityTrainingEvaluation(getAbility(), requirements, isPoolAbility());
    }
}
