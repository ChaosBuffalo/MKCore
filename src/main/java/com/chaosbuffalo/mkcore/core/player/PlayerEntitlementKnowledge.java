package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entitlements.EntitlementInstance;
import com.chaosbuffalo.mkcore.core.entity.EntityEntitlementsKnowledge;

public class PlayerEntitlementKnowledge extends EntityEntitlementsKnowledge {

    public PlayerEntitlementKnowledge(MKPlayerData entityData) {
        super(entityData);
    }

    private MKPlayerData getPlayerData() {
        return (MKPlayerData) entityData;
    }

    @Override
    protected void broadcastChange(EntitlementInstance instance) {
        super.broadcastChange(instance);
        getPlayerData().getEntitlementDispatcher().onRecordUpdated(instance);
    }
}
