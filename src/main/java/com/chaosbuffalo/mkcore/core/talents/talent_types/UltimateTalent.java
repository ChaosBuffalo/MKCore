package com.chaosbuffalo.mkcore.core.talents.talent_types;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.talents.IAbilityTalent;
import com.chaosbuffalo.mkcore.core.talents.MKTalent;
import com.chaosbuffalo.mkcore.core.talents.TalentType;
import net.minecraft.util.ResourceLocation;

public class UltimateTalent extends MKTalent implements IAbilityTalent<MKAbility> {
    private final MKAbility ability;

    public UltimateTalent(ResourceLocation name, MKAbility ability) {
        super(name);
        this.ability = ability;
    }

    @Override
    public MKAbility getAbility() {
        return ability;
    }

    @Override
    public TalentType<?> getTalentType() {
        return TalentType.ULTIMATE;
    }
}
