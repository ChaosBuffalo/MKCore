package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

import static com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager.RAW_EFFECT;

public class WriteAnimationPacket {

    protected final ResourceLocation name;
    protected final ParticleAnimation anim;

    public WriteAnimationPacket(ResourceLocation name, ParticleAnimation anim) {
        this.name = name;
        this.anim = anim;
    }

    public WriteAnimationPacket(PacketBuffer buf) {
        this.name = buf.readResourceLocation();
        this.anim = ParticleAnimation.deserializeFromDynamic(RAW_EFFECT, new Dynamic<>(NBTDynamicOps.INSTANCE,
                buf.readCompoundTag()));
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeResourceLocation(name);
        INBT dyn = anim.serialize(NBTDynamicOps.INSTANCE);
        if (dyn instanceof CompoundNBT) {
            buf.writeCompoundTag((CompoundNBT) dyn);
        } else {
            throw new RuntimeException(String.format("Particle Animation %s did not serialize to a CompoundNBT!", name));
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            if (ctx.getSender() != null && ctx.getSender().isCreative()) {
                MKCore.getAnimationManager().writeAnimationToWorldGenerated(name, anim);
            }
        });
        ctx.setPacketHandled(true);
    }
}
