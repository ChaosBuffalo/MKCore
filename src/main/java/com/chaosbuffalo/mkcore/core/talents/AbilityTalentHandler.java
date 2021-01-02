package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.PlayerAbilityKnowledge;
import net.minecraft.util.ResourceLocation;

public abstract class AbilityTalentHandler extends TalentTypeHandler {
    public AbilityTalentHandler(MKPlayerData playerData) {
        super(playerData);
    }

    @Override
    public void onRecordUpdated(TalentRecord record) {
        MKAbility ability = TalentManager.getTalentAbility(record.getNode().getTalent().getRegistryName());
        if (ability == null)
            return;

        if (!record.isKnown()) {
            onUnknownAbilityUpdated(record, ability);
        } else {
            onKnownAbilityUpdated(record, ability);
        }
    }

    @Override
    public void onRecordLoaded(TalentRecord record) {
        if (record.isKnown()) {
            MKAbility ability = TalentManager.getTalentAbility(record.getNode().getTalent().getRegistryName());
            if (ability == null)
                return;
            tryLearn(ability);
        }
    }

    protected void onUnknownAbilityUpdated(TalentRecord record, MKAbility ability) {
        playerData.getAbilities().unlearnAbility(ability.getAbilityId());
    }

    protected void onKnownAbilityUpdated(TalentRecord record, MKAbility ability) {
        tryLearn(ability);
    }

    protected void tryLearn(MKAbility ability) {
        PlayerAbilityKnowledge abilityKnowledge = playerData.getAbilities();
        if (!abilityKnowledge.knowsAbility(ability.getAbilityId())) {
            abilityKnowledge.learnAbility(ability, AbilitySource.TALENT);
        }
    }

    public abstract void onSlotChanged(int index, ResourceLocation oldAbilityId, ResourceLocation newAbilityId);
}
