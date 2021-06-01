package com.chaosbuffalo.mkcore.core.talents.handlers;

import com.chaosbuffalo.mkcore.core.AbilitySlot;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.IActiveAbilityGroup;
import com.chaosbuffalo.mkcore.core.talents.TalentRecord;
import com.chaosbuffalo.mkcore.core.talents.TalentTypeHandler;

public class SlotTalentTypeHandler extends TalentTypeHandler {
    private final AbilitySlot slotType;

    public SlotTalentTypeHandler(MKPlayerData playerData, AbilitySlot slotType) {
        super(playerData);
        this.slotType = slotType;
    }

    @Override
    public void onRecordUpdated(TalentRecord record) {
        IActiveAbilityGroup abilityGroup = playerData.getAbilityLoadout().getAbilityGroup(getSlotType());
        if (record.isKnown()){
            abilityGroup.setSlots(abilityGroup.getCurrentSlotCount() + 1);
        } else {
            abilityGroup.setSlots(abilityGroup.getCurrentSlotCount() - 1);
        }
    }

    protected AbilitySlot getSlotType() {
        return slotType;
    };
}
