package com.chaosbuffalo.mkcore.sync;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nullable;

public class SyncEntity<T extends Entity> implements ISyncObject {
    private final String name;
    @Nullable
    private T value;
    private final Class<T> clazz;
    private boolean dirty;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public SyncEntity(String name, T value, Class<T> clazz) {
        this.name = name;
        this.clazz = clazz;
        set(value);
    }

    public void set(T value) {
        this.value = value;
        this.dirty = true;
        parentNotifier.notifyUpdate(this);
    }

    @Nullable
    public T get() {
        return value;
    }

    @Override
    public void setNotifier(ISyncNotifier notifier) {
        parentNotifier = notifier;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void deserializeUpdate(CompoundNBT tag) {
        if (tag.contains(name)) {
            int id = tag.getInt(name);
            if (id != -1) {
                Entity ent = ClientHandler.handleClient(tag.getId());
                if (clazz.isInstance(ent)) {
                    value = clazz.cast(ent);
                } else {
                    value = null;
                }
            } else {
                value = null;
            }
        }
    }

    @Override
    public void serializeUpdate(CompoundNBT tag) {
        if (dirty) {
            serializeFull(tag);
            dirty = false;
        }
    }

    @Override
    public void serializeFull(CompoundNBT tag) {
        tag.putInt(name, value != null ? value.getEntityId() : -1);
        dirty = false;
    }

    static class ClientHandler {
        public static Entity handleClient(int entityId) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.world == null)
                return null;
            return mc.world.getEntityByID(entityId);
        }
    }
}

