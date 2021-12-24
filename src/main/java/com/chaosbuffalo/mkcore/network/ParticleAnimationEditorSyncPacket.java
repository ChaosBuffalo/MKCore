package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

import static com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager.RAW_EFFECT;

public class ParticleAnimationEditorSyncPacket {

    protected final ParticleAnimation anim;
    protected final int currentKeyFrame;


    public ParticleAnimationEditorSyncPacket(ParticleAnimation anim, int currentKeyFrame) {
        this.anim = anim;
        this.currentKeyFrame = currentKeyFrame;
    }


    public ParticleAnimationEditorSyncPacket(PacketBuffer buf) {
        this.currentKeyFrame = buf.readInt();
        this.anim = ParticleAnimation.deserializeFromDynamic(RAW_EFFECT, new Dynamic<>(NBTDynamicOps.INSTANCE,
                buf.readCompoundTag()));
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(currentKeyFrame);

        INBT dyn = anim.serialize(NBTDynamicOps.INSTANCE);
        if (dyn instanceof CompoundNBT) {
            buf.writeCompoundTag((CompoundNBT) dyn);
        } else {
            throw new RuntimeException(String.format("Particle Animation %s did not serialize to a CompoundNBT!", anim));
        }
    }


    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            if (ctx.getSender() != null && ctx.getSender().isCreative()) {
                MKCore.getPlayer(ctx.getSender()).ifPresent(data -> data.getEditor().getParticleEditorData()
                        .update(anim, currentKeyFrame, false));
            }
        });
        ctx.setPacketHandled(true);
    }
}
