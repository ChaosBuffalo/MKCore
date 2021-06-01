package com.chaosbuffalo.mkcore.core.talents.talent_types;

import com.chaosbuffalo.mkcore.core.talents.MKTalent;
import com.chaosbuffalo.mkcore.core.talents.TalentType;
import net.minecraft.util.ResourceLocation;

public class SlotTalent extends MKTalent {

    private final TalentType<?> talentType;

    public SlotTalent(ResourceLocation name, TalentType<?> talentType) {
        super(name);
        this.talentType = talentType;
    }

    @Override
    public TalentType<?> getTalentType() {
        return talentType;
    }
}
