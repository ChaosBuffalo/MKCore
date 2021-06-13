package com.chaosbuffalo.mkcore.core.talents.talent_types;

import com.chaosbuffalo.mkcore.core.talents.MKTalent;
import com.chaosbuffalo.mkcore.core.talents.TalentType;
import net.minecraft.util.ResourceLocation;

public class SlotCountTalent extends MKTalent {

    private final TalentType<?> talentType;

    public SlotCountTalent(ResourceLocation name, TalentType<?> talentType) {
        super(name);
        this.talentType = talentType;
    }

    @Override
    public TalentType<?> getTalentType() {
        return talentType;
    }
}
