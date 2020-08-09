package com.chaosbuffalo.mkcore.abilities.ai.conditions;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityDecisionContext;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityTargetingDecision;
import net.minecraft.entity.LivingEntity;

import javax.annotation.Nonnull;

public class StandardUseCondition extends AbilityUseCondition {
    private final AbilityTargetingDecision.MovementSuggestion movementSuggestion;

    public StandardUseCondition(MKAbility ability) {
        super(ability);
        movementSuggestion = AbilityTargetingDecision.MovementSuggestion.KITE;
    }

    @Nonnull
    @Override
    public AbilityTargetingDecision getDecision(AbilityDecisionContext context) {
        LivingEntity threatTarget = context.getThreatTarget();
        if (threatTarget != null) {
            return new AbilityTargetingDecision(threatTarget, movementSuggestion, getAbility());
        }
        return AbilityTargetingDecision.UNDECIDED;
    }
}
