package com.chaosbuffalo.mkcore.core.talents.talent_types;

import com.chaosbuffalo.mkcore.abilities.PassiveTalentAbility;
import com.chaosbuffalo.mkcore.core.talents.IAbilityTalent;
import com.chaosbuffalo.mkcore.core.talents.MKTalent;
import com.chaosbuffalo.mkcore.core.talents.TalentType;
import net.minecraft.util.ResourceLocation;

public class PassiveTalent extends MKTalent implements IAbilityTalent<PassiveTalentAbility> {
    private final PassiveTalentAbility ability;

    public PassiveTalent(ResourceLocation name, PassiveTalentAbility ability) {
        super(name);
        this.ability = ability;
    }

    @Override
    public PassiveTalentAbility getAbility() {
        return ability;
    }

    @Override
    public TalentType<?> getTalentType() {
        return TalentType.PASSIVE;
    }

    @Override
    public String toString() {
        return "PassiveTalent{" +
                "ability=" + ability +
                '}';
    }
}
