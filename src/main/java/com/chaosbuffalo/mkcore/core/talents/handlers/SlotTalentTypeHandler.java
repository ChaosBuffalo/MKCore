package com.chaosbuffalo.mkcore.core.talents.handlers;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entitlements.EntitlementInstance;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mkcore.core.talents.TalentRecord;
import com.chaosbuffalo.mkcore.core.talents.TalentTypeHandler;
import com.chaosbuffalo.mkcore.core.talents.nodes.SlotCountTalentNode;
import com.chaosbuffalo.mkcore.init.CoreEntitlements;

import javax.annotation.Nullable;

public class SlotTalentTypeHandler extends TalentTypeHandler {
    private final AbilityType slotType;

    public SlotTalentTypeHandler(MKPlayerData playerData, AbilityType slotType) {
        super(playerData);
        this.slotType = slotType;
    }

    @Override
    public void onRecordUpdated(TalentRecord record) {
        if (record.getNode() instanceof SlotCountTalentNode){
            SlotCountTalentNode slotNode = (SlotCountTalentNode) record.getNode();
            MKEntitlement entitlement = getEntitlementForSlotType(slotType);
            if (entitlement != null){
                if (record.isKnown()){
                    playerData.getKnowledge().getEntitlementsKnowledge().addEntitlement(new EntitlementInstance(entitlement, slotNode.getNodeId()), true);
                } else {
                    playerData.getKnowledge().getEntitlementsKnowledge().removeEntitlementByUUID(slotNode.getNodeId());
                }
            } else {
                MKCore.LOGGER.error("Talent node has slot type without entitlement {}, {}", slotType, this);
            }
        }
    }

    @Nullable
    private MKEntitlement getEntitlementForSlotType(AbilityType slotType){
        switch (slotType){
            case Basic:
                return CoreEntitlements.BasicAbilitySlotCount;
            case Passive:
                return CoreEntitlements.PassiveAbilitySlotCount;
            case Ultimate:
                return CoreEntitlements.UltimateAbilitySlotCount;
            default:
                return null;
        }
    }

    protected AbilityType getSlotType() {
        return slotType;
    };
}
