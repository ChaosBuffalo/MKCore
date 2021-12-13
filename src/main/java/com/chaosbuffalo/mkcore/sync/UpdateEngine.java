package com.chaosbuffalo.mkcore.sync;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public abstract class UpdateEngine {
    protected final SyncGroup publicUpdater = new SyncGroup();
    protected boolean readyForUpdates = false;

    public void addPublic(ISyncObject syncObject) {
        publicUpdater.add(syncObject);
        if (syncObject instanceof SyncGroup) {
            ((SyncGroup) syncObject).forceDirty();
        }
    }

    public void removePublic(ISyncObject syncObject) {
        publicUpdater.remove(syncObject);
        if (syncObject instanceof SyncGroup) {
            ((SyncGroup) syncObject).forceDirty();
        }
    }

    public void addPrivate(ISyncObject syncObject) {
    }

    public void removePrivate(ISyncObject syncObject) {
    }

    public abstract void syncUpdates();

    public abstract void serializeUpdate(CompoundNBT updateTag, boolean fullSync, boolean privateUpdate);

    public abstract void deserializeUpdate(CompoundNBT updateTag, boolean privateUpdate);

    public abstract void sendAll(ServerPlayerEntity otherPlayer);


}
