package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MKItemAttackPacket {

    private int entityId;

    public MKItemAttackPacket(Entity entity){
        this.entityId = entity.getEntityId();
    }

    public MKItemAttackPacket(PacketBuffer buf) {
        entityId = buf.readInt();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(entityId);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity entity = ctx.getSender();
            if (entity == null) {
                return;
            }
            Entity target = entity.getServerWorld().getEntityByID(entityId);
            double reach = entity.getAttribute(MKAttributes.ATTACK_REACH).getValue();
            if (target != null) {
                if (entity.getDistanceSq(target) <= reach * reach){
                    entity.attackTargetEntityWithCurrentItem(target);
                }
            }
        });
        ctx.setPacketHandled(true);
    }

}
