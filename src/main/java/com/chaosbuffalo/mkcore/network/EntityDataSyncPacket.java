package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.entities.IUpdateEngineProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class EntityDataSyncPacket {

    private final int targetID;
    private final CompoundNBT updateTag;

    public EntityDataSyncPacket(int targetID, CompoundNBT updateTag) {
        this.targetID = targetID;
        this.updateTag = updateTag;
    }

    public EntityDataSyncPacket(PacketBuffer buffer) {
        targetID = buffer.readInt();
        updateTag = buffer.readCompoundTag();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(targetID);
        buffer.writeCompoundTag(updateTag);
//        MKCore.LOGGER.info("sync toBytes priv:{} {}", privateUpdate, updateTag);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        World world = Minecraft.getInstance().world;
        if (world == null) {
            return;
        }
        Entity target = world.getEntityByID(targetID);
        if (target instanceof IUpdateEngineProvider){
            ((IUpdateEngineProvider) target).getUpdateEngine().deserializeUpdate(updateTag, false);
        }
    }

    public void handle(Supplier< NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(this::handleClient);
        ctx.setPacketHandled(true);
    }

    public String toString() {
        return String.format("[tag: %s]", updateTag);
    }

}
