package com.chaosbuffalo.mkcore.abilities.training;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;


// TODO: Might be a good candidate for a record in J16
public class AbilityRequirementEvaluation {
    private final ITextComponent requirementDescription;
    private final boolean isMet;

    public AbilityRequirementEvaluation(ITextComponent description, boolean isMet) {
        this.requirementDescription = description;
        this.isMet = isMet;
    }

    public boolean isMet() {
        return isMet;
    }

    public ITextComponent description() {
        return requirementDescription;
    }

    public void write(PacketBuffer buffer) {
        buffer.writeTextComponent(requirementDescription);
        buffer.writeBoolean(isMet);
    }

    public static AbilityRequirementEvaluation read(PacketBuffer buffer) {
        return new AbilityRequirementEvaluation(buffer.readTextComponent(), buffer.readBoolean());
    }
}
