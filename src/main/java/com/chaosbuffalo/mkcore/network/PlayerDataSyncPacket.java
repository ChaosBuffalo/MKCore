package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PlayerDataSyncPacket {

    private final UUID targetUUID;
    private final boolean privateUpdate;
    private final CompoundNBT updateTag;

    public PlayerDataSyncPacket(UUID targetUUID, CompoundNBT updateTag, boolean privateUpdate) {
        this.targetUUID = targetUUID;
        this.privateUpdate = privateUpdate;
        this.updateTag = updateTag;
    }

    public PlayerDataSyncPacket(PacketBuffer buffer) {
        targetUUID = buffer.readUniqueId();
        privateUpdate = buffer.readBoolean();
        updateTag = buffer.readCompoundTag();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeUniqueId(targetUUID);
        buffer.writeBoolean(privateUpdate);
        buffer.writeCompoundTag(updateTag);
//        MKCore.LOGGER.info("sync toBytes priv:{} {}", privateUpdate, updateTag);
    }

    public static void handle(PlayerDataSyncPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> ClientHandler.handleClient(packet));
        ctx.setPacketHandled(true);
    }

    static class ClientHandler {
        public static void handleClient(PlayerDataSyncPacket packet) {
            World world = Minecraft.getInstance().world;
            if (world == null) {
                return;
            }
            PlayerEntity entity = world.getPlayerByUuid(packet.targetUUID);
            if (entity == null)
                return;

            MKCore.getPlayer(entity).ifPresent(cap ->
                    cap.getUpdateEngine().deserializeUpdate(packet.updateTag, packet.privateUpdate));
        }
    }

    public String toString() {
        return String.format("[priv: %b, tag: %s]", privateUpdate, updateTag);
    }
}
