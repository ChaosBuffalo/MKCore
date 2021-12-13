package com.chaosbuffalo.mkcore.sync;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.network.EntityDataSyncPacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public class EntityUpdateEngine extends UpdateEngine{

    private final Entity entity;

    public EntityUpdateEngine(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void syncUpdates() {
        if (entity.getEntityWorld().isRemote){
            return;
        }
        if (publicUpdater.isDirty()) {
            EntityDataSyncPacket packet = getUpdateMessage();
            MKCore.LOGGER.info("sending public dirty update {} for {}", packet, entity);
            PacketHandler.sendToTracking(packet, entity);
        }
    }

    private EntityDataSyncPacket getUpdateMessage() {
        CompoundNBT tag = new CompoundNBT();
        serializeUpdate(tag, false, false);
        return new EntityDataSyncPacket(entity.getEntityId(), tag);
    }

    @Override
    public void serializeUpdate(CompoundNBT updateTag, boolean fullSync, boolean privateUpdate) {
        if (fullSync) {
            publicUpdater.serializeFull(updateTag);
        } else {
            publicUpdater.serializeUpdate(updateTag);
        }
    }

    @Override
    public void deserializeUpdate(CompoundNBT updateTag, boolean privateUpdate) {
        publicUpdater.deserializeUpdate(updateTag);
    }

    @Override
    public void sendAll(ServerPlayerEntity otherPlayer) {
        if (entity.getEntityWorld().isRemote)
            return;
        CompoundNBT tag = new CompoundNBT();
        publicUpdater.serializeFull(tag);
        EntityDataSyncPacket packet = new EntityDataSyncPacket(entity.getEntityId(), tag);
        MKCore.LOGGER.info("sending full sync {} for {} to {}", packet, entity, otherPlayer);
        PacketHandler.sendMessage(packet, otherPlayer);
    }
}
