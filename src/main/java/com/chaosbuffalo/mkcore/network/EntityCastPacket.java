package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class EntityCastPacket {

    private final int entityId;
    private ResourceLocation abilityId;
    private int castTicks;
    private final CastAction action;

    enum CastAction {
        START,
        INTERRUPT
    }

    public EntityCastPacket(IMKEntityData entityData, ResourceLocation abilityId, int castTicks) {
        entityId = entityData.getEntity().getEntityId();
        this.abilityId = abilityId;
        this.castTicks = castTicks;
        action = CastAction.START;
    }

    public EntityCastPacket(IMKEntityData entityData, CastAction action) {
        entityId = entityData.getEntity().getEntityId();
        this.action = action;
    }

    public static EntityCastPacket start(IMKEntityData entityData, ResourceLocation abilityId, int castTicks) {
        return new EntityCastPacket(entityData, abilityId, castTicks);
    }

    public static EntityCastPacket interrupt(IMKEntityData entityData) {
        return new EntityCastPacket(entityData, CastAction.INTERRUPT);
    }

    public EntityCastPacket(PacketBuffer buffer) {
        entityId = buffer.readInt();
        action = buffer.readEnumValue(CastAction.class);
        if (action == CastAction.START) {
            abilityId = buffer.readResourceLocation();
            castTicks = buffer.readInt();
        }
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(entityId);
        buffer.writeEnumValue(action);
        if (action == CastAction.START) {
            buffer.writeResourceLocation(abilityId);
            buffer.writeInt(castTicks);
        }
    }

    public static void handle(EntityCastPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> ClientHandler.handleClient(packet));
        ctx.setPacketHandled(true);
    }

    static class ClientHandler {
        public static void handleClient(EntityCastPacket packet) {
            World world = Minecraft.getInstance().world;
            if (world == null)
                return;

            Entity entity = world.getEntityByID(packet.entityId);
            if (entity == null)
                return;

            MKCore.getEntityData(entity).ifPresent(entityData -> {
                if (packet.action == CastAction.START) {
                    entityData.getAbilityExecutor().startCastClient(packet.abilityId, packet.castTicks);
                } else if (packet.action == CastAction.INTERRUPT) {
                    entityData.getAbilityExecutor().interruptCast();
                }
            });
        }
    }
}
