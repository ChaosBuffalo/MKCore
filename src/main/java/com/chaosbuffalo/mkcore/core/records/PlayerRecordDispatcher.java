package com.chaosbuffalo.mkcore.core.records;

import com.chaosbuffalo.mkcore.core.MKPlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public abstract class PlayerRecordDispatcher {
    protected final MKPlayerData playerData;
    protected final Map<IRecordType<?>, IRecordTypeHandler<?>> typeHandlerMap = new HashMap<>();

    public PlayerRecordDispatcher(MKPlayerData playerData) {
        this.playerData = playerData;
    }

    protected <T extends IRecordInstance> IRecordTypeHandler<T> getRecordHandler(IRecordInstance record) {
        return (IRecordTypeHandler<T>) getTypeHandler(record.getRecordType());
    }

    public <T extends IRecordInstance> void onRecordUpdated(T record) {
        getRecordHandler(record).onRecordUpdated(record);
    }

    public <T extends IRecordTypeHandler<?>> T getTypeHandler(IRecordType<T> type) {
        //noinspection unchecked
        return (T) typeHandlerMap.computeIfAbsent(type, t -> type.createTypeHandler(playerData));
    }

    protected abstract Stream<? extends IRecordInstance> getRecordStream();

    public void onPersonaActivated() {
        typeHandlerMap.clear();

        getRecordStream().forEach(r -> getRecordHandler(r).onRecordLoaded(r));

        typeHandlerMap.values().forEach(IRecordTypeHandler::onPersonaActivated);
    }

    public void onPersonaDeactivated() {
        typeHandlerMap.values().forEach(IRecordTypeHandler::onPersonaDeactivated);
    }

    public void onJoinWorld() {
        typeHandlerMap.values().forEach(IRecordTypeHandler::onJoinWorld);
    }
}
