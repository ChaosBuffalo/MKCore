package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.sync.ISyncObject;
import com.chaosbuffalo.mkcore.sync.SyncGroup;
import com.chaosbuffalo.mkcore.sync.UpdateEngine;

public class PlayerSyncComponent {

    private final SyncGroup publicUpdater = new SyncGroup();
    private final SyncGroup privateUpdater = new SyncGroup();

    public PlayerSyncComponent() {
    }

    public PlayerSyncComponent(String name) {
        publicUpdater.setNestingName(name);
        privateUpdater.setNestingName(name);
    }

    public void attach(UpdateEngine engine) {
        engine.addPublic(publicUpdater);
        engine.addPrivate(privateUpdater);
    }

    public void detach(UpdateEngine engine) {
        engine.removePublic(publicUpdater);
        engine.removePrivate(privateUpdater);
    }

    public void addChild(PlayerSyncComponent component) {
        addPublic(component.publicUpdater);
        addPrivate(component.privateUpdater);
    }

    public void addPublic(ISyncObject syncObject) {
        publicUpdater.add(syncObject);
    }

    public void addPrivate(ISyncObject syncObject) {
        addPrivate(syncObject, false);
    }

    public void addPrivate(ISyncObject syncObject, boolean forceUpdate) {
        privateUpdater.add(syncObject);
        if (forceUpdate) {
            privateUpdater.forceDirty();
        }
    }
}
