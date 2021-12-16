package com.chaosbuffalo.mkcore.core.records;

public interface IRecordTypeHandler<T extends IRecordInstance> {
    default void onJoinWorld() {

    }

    default void onPersonaActivated() {

    }

    default void onPersonaDeactivated() {

    }

    default void onRecordUpdated(T record) {

    }

    default void onRecordLoaded(T record) {

    }

    default void onRecordAdded(T record) {
        onRecordUpdated(record);
    }

    default void onRecordRemoved(T record) {
        onRecordUpdated(record);
    }
}
