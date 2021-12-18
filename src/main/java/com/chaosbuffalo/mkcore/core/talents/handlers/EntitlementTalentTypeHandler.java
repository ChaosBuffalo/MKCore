package com.chaosbuffalo.mkcore.core.talents.handlers;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entitlements.EntitlementInstance;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mkcore.core.talents.TalentRecord;
import com.chaosbuffalo.mkcore.core.talents.TalentTypeHandler;
import com.chaosbuffalo.mkcore.core.talents.nodes.UUIDTalentNode;
import com.chaosbuffalo.mkcore.init.CoreEntitlements;

import javax.annotation.Nullable;

public class EntitlementTalentTypeHandler extends TalentTypeHandler {
    private final MKEntitlement entitlement;

    public EntitlementTalentTypeHandler(MKPlayerData playerData, MKEntitlement entitlement) {
        super(playerData);
        this.entitlement = entitlement;
    }

    @Override
    public void onRecordUpdated(TalentRecord record) {
        if (record.getNode() instanceof UUIDTalentNode){
            UUIDTalentNode slotNode = (UUIDTalentNode) record.getNode();
            if (getEntitlement() != null){
                if (record.isKnown()){
                    playerData.getKnowledge().getEntitlementsKnowledge().addEntitlement(new EntitlementInstance(getEntitlement(), slotNode.getNodeId()), true);
                } else {
                    playerData.getKnowledge().getEntitlementsKnowledge().removeEntitlementByUUID(slotNode.getNodeId());
                }
            } else {
                MKCore.LOGGER.error("Talent node has slot type without entitlement {}", this);
            }
        }
    }

    public MKEntitlement getEntitlement() {
        return entitlement;
    }
}
