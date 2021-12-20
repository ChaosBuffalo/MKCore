package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.ActiveAbilityGroup;
import net.minecraft.util.ResourceLocation;

public class ActiveTalentAbilityGroup extends ActiveAbilityGroup {
    protected TalentType<?> talentType;

    public ActiveTalentAbilityGroup(MKPlayerData playerData, String name, AbilityGroupId group, TalentType<?> talentType) {
        super(playerData, name, group);
        this.talentType = talentType;
    }

    public TalentType<?> getTalentType() {
        return talentType;
    }

    private boolean knowsTalent(ResourceLocation talentId) {
        return playerData.getTalents().getKnownTalentIds(talentType).contains(talentId);
    }

    public void setActiveTalent(int index, ResourceLocation talentId) {
        if (talentId.equals(MKCoreRegistry.INVALID_TALENT)) {
            clearSlot(index);
            return;
        }

        if (knowsTalent(talentId)) {
            MKAbility ability = TalentManager.getTalentAbility(talentId);
            if (ability == null) {
                MKCore.LOGGER.error("ActiveTalentAbilityContainer.setActiveTalent {} {} - talent does provide ability!", index, talentId);
                return;
            }

            setSlot(index, ability.getAbilityId());
        } else {
            MKCore.LOGGER.error("ActiveTalentAbilityContainer.setActiveTalent {} {} - player does not know talent!", index, talentId);
        }
    }
}
