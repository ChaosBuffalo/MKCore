package com.chaosbuffalo.mkcore.core.talents.handlers;

import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.PlayerAbilityKnowledge;
import com.chaosbuffalo.mkcore.core.talents.TalentManager;
import com.chaosbuffalo.mkcore.core.talents.TalentRecord;
import com.chaosbuffalo.mkcore.core.talents.TalentTypeHandler;

public class AbilityGrantTalentHandler extends TalentTypeHandler {
    public AbilityGrantTalentHandler(MKPlayerData playerData) {
        super(playerData);
    }

    @Override
    public void onRecordUpdated(TalentRecord record) {
        MKAbility ability = TalentManager.getTalentAbility(record.getNode().getTalent().getTalentId());
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
            MKAbility ability = TalentManager.getTalentAbility(record.getNode().getTalent().getTalentId());
            if (ability == null)
                return;
            tryLearn(ability);
        }
    }

    protected void onUnknownAbilityUpdated(TalentRecord record, MKAbility ability) {
        playerData.getAbilities().unlearnAbility(ability.getAbilityId(), AbilitySource.TALENT);
    }

    protected void onKnownAbilityUpdated(TalentRecord record, MKAbility ability) {
        tryLearn(ability);
    }

    protected void tryLearn(MKAbility ability) {
        PlayerAbilityKnowledge abilityKnowledge = playerData.getAbilities();
        if (!abilityKnowledge.knowsAbility(ability.getAbilityId(), AbilitySource.TALENT)) {
            abilityKnowledge.learnAbility(ability, AbilitySource.TALENT);
        }
    }
}
