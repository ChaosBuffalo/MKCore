package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MKParticleEffectEditorSpawnPacket extends MKParticleEffectSpawnPacket {

    public MKParticleEffectEditorSpawnPacket(Vector3d posVec, ParticleAnimation anim) {
        super(posVec, anim);
    }

    public MKParticleEffectEditorSpawnPacket(PacketBuffer buffer) {
        super(buffer);
    }

    public static void handle(MKParticleEffectEditorSpawnPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            if (ctx.getSender() != null && ctx.getSender().isCreative()) {
                PacketHandler.sendToTrackingAndSelf(new MKParticleEffectSpawnPacket(packet.xPos, packet.yPos, packet.zPos, packet.anim),
                        ctx.getSender());
            }
        });
        ctx.setPacketHandled(true);
    }
}
