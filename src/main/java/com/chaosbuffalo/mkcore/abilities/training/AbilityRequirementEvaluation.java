package com.chaosbuffalo.mkcore.abilities.training;

import net.minecraft.util.text.ITextComponent;


public class AbilityRequirementEvaluation {
    public final ITextComponent requirementDescription;
    public final boolean isMet;

    public AbilityRequirementEvaluation(ITextComponent description, boolean isMet) {
        this.requirementDescription = description;
        this.isMet = isMet;
    }

}
