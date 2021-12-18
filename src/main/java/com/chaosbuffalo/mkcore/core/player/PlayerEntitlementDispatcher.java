package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.records.IRecordInstance;
import com.chaosbuffalo.mkcore.core.records.PlayerRecordDispatcher;

import java.util.stream.Stream;

public class PlayerEntitlementDispatcher extends PlayerRecordDispatcher {
    public PlayerEntitlementDispatcher(MKPlayerData playerData) {
        super(playerData);
    }

    protected Stream<? extends IRecordInstance> getRecordStream() {
        return playerData.getKnowledge()
                .getEntitlementsKnowledge()
                .getInstanceStream();
    }

    @Override
    public void onPersonaActivated() {
        playerData.getKnowledge().getEntitlementsKnowledge().broadcastLoaded();
    }
}
