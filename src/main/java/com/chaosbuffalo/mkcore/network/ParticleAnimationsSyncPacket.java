package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ParticleAnimationsSyncPacket {
    private final Map<ResourceLocation, CompoundNBT> data;

    public ParticleAnimationsSyncPacket(Map<ResourceLocation, ParticleAnimation> animations) {
        data = new HashMap<>();
        for (Map.Entry<ResourceLocation, ParticleAnimation> entry : animations.entrySet()) {
            INBT dyn = entry.getValue().serialize(NBTDynamicOps.INSTANCE);
            if (dyn instanceof CompoundNBT) {
                data.put(entry.getKey(), (CompoundNBT) dyn);
            } else {
                throw new RuntimeException(String.format(
                        "Particle Animation %s did not serialize to a CompoundNBT!", entry.getKey()));
            }
        }
    }


    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(data.size());
        for (Map.Entry<ResourceLocation, CompoundNBT> animData : data.entrySet()) {
            buffer.writeResourceLocation(animData.getKey());
            buffer.writeCompoundTag(animData.getValue());
        }
    }

    public ParticleAnimationsSyncPacket(PacketBuffer buffer) {
        int count = buffer.readInt();
        data = new HashMap<>();
        for (int i = 0; i < count; i++) {
            ResourceLocation animName = buffer.readResourceLocation();
            CompoundNBT animData = buffer.readCompoundTag();
            data.put(animName, animData);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        MKCore.LOGGER.debug("Handling particle animation sync packet");
        ctx.enqueueWork(() -> {
            for (Map.Entry<ResourceLocation, CompoundNBT> animData : data.entrySet()) {
                ParticleAnimation anim = ParticleAnimation.deserializeFromDynamic(animData.getKey(),
                        new Dynamic<>(NBTDynamicOps.INSTANCE, animData.getValue()));
                ParticleAnimationManager.ANIMATIONS.put(animData.getKey(), anim);
            }
        });
        ctx.setPacketHandled(true);
    }
}
