package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.records.IRecordInstance;
import com.chaosbuffalo.mkcore.core.records.PlayerRecordDispatcher;

import java.util.stream.Stream;

public class PlayerTalentDispatcher extends PlayerRecordDispatcher {

    public PlayerTalentDispatcher(MKPlayerData playerData) {
        super(playerData);
    }

    @Override
    protected Stream<? extends IRecordInstance> getRecordStream() {
        return playerData.getKnowledge()
                .getTalentKnowledge()
                .getKnownTalentsStream();
    }
}
