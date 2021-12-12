package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MKParticleEffectEditorSpawnPacket extends MKParticleEffectSpawnPacket{

    public MKParticleEffectEditorSpawnPacket(Vector3d posVec, ParticleAnimation anim) {
        super(posVec, anim);
    }

    public MKParticleEffectEditorSpawnPacket(PacketBuffer buffer){
        super(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            if (ctx.getSender().isCreative()){
                PacketHandler.sendToTrackingAndSelf(new MKParticleEffectSpawnPacket(xPos, yPos, zPos, anim),
                        ctx.getSender());
            }
        });
        ctx.setPacketHandled(true);
    }
}
