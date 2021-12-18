package com.chaosbuffalo.mkcore.core.talents.talent_types;

import com.chaosbuffalo.mkcore.core.talents.MKTalent;
import com.chaosbuffalo.mkcore.core.talents.TalentNode;
import com.chaosbuffalo.mkcore.core.talents.TalentType;
import com.chaosbuffalo.mkcore.core.talents.nodes.UUIDTalentNode;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.ResourceLocation;

public class UUIDTalent extends MKTalent {

    private final TalentType<?> talentType;

    public UUIDTalent(ResourceLocation name, TalentType<?> talentType) {
        super(name);
        this.talentType = talentType;
    }

    @Override
    public TalentType<?> getTalentType() {
        return talentType;
    }

    @Override
    public <T> TalentNode createNode(Dynamic<T> dynamic) {
        return new UUIDTalentNode(this, dynamic);
    }
}
