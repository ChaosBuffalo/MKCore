package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.fx.particles.MKParticleData;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class MKParticleEffectSpawnPacket {
    private final double xPos;
    private final double yPos;
    private final double zPos;
    private final int motionType;
    private final double speed;
    private final int count;
    private final double radiusX;
    private final double radiusY;
    private final double radiusZ;
    private final ParticleType<MKParticleData> particleType;
    private final int data;
    private final double headingX;
    private final double headingY;
    private final double headingZ;
    private final ParticleAnimation anim;
    private final ResourceLocation animName;
    private final boolean hasRaw;
    public static final ResourceLocation RAW_EFFECT = new ResourceLocation(MKCore.MOD_ID, "particle_anim.raw_effect");


    public MKParticleEffectSpawnPacket(ParticleType<MKParticleData> particleType, int motionType, int count, int data,
                                       double xPos, double yPos, double zPos,
                                       double radiusX, double radiusY, double radiusZ,
                                       double speed, double headingX, double headingY, double headingZ,
                                       ParticleAnimation anim) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
        this.motionType = motionType;
        this.count = count;
        this.speed = speed;
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.radiusZ = radiusZ;
        this.particleType = particleType;
        this.data = data;
        this.headingX = headingX;
        this.headingY = headingY;
        this.headingZ = headingZ;
        this.anim = anim;
        this.hasRaw = true;
        this.animName = RAW_EFFECT;
    }

    public MKParticleEffectSpawnPacket(ParticleType<MKParticleData> particleType, int motionType, int count, int data,
                                       double xPos, double yPos, double zPos,
                                       double radiusX, double radiusY, double radiusZ,
                                       double speed, double headingX, double headingY, double headingZ,
                                       ResourceLocation animName) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
        this.motionType = motionType;
        this.count = count;
        this.speed = speed;
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.radiusZ = radiusZ;
        this.particleType = particleType;
        this.data = data;
        this.headingX = headingX;
        this.headingY = headingY;
        this.headingZ = headingZ;
        this.anim = null;
        this.animName = animName;
        this.hasRaw = false;
    }

    public MKParticleEffectSpawnPacket(ParticleType<MKParticleData> particleType, int motionType, int count, int data,
                                       double xPos, double yPos, double zPos,
                                       double radiusX, double radiusY, double radiusZ,
                                       double speed, Vector3d headingVec, ResourceLocation animName) {
        this(particleType, motionType, count, data,
                xPos, yPos, zPos,
                radiusX, radiusY, radiusZ, speed,
                headingVec.x, headingVec.y, headingVec.z, animName);
    }

    public MKParticleEffectSpawnPacket(ParticleType<MKParticleData> particleType, int motionType, int count, int data,
                                       double xPos, double yPos, double zPos,
                                       double radiusX, double radiusY, double radiusZ,
                                       double speed, Vector3d headingVec, ParticleAnimation anim) {
        this(particleType, motionType, count, data,
                xPos, yPos, zPos,
                radiusX, radiusY, radiusZ, speed,
                headingVec.x, headingVec.y, headingVec.z, anim);
    }

    public MKParticleEffectSpawnPacket(ParticleType<MKParticleData> particleType, int motionType, int count, int data,
                                       Vector3d posVec,
                                       double radiusX, double radiusY, double radiusZ,
                                       double speed, Vector3d headingVec, ParticleAnimation anim) {
        this(particleType, motionType, count, data, posVec.x, posVec.y, posVec.z, radiusX,
                radiusY, radiusZ, speed, headingVec.x, headingVec.y, headingVec.z, anim);
    }

    public MKParticleEffectSpawnPacket(PacketBuffer buf) {
        this.particleType = (ParticleType<MKParticleData>) ForgeRegistries.PARTICLE_TYPES.getValue(buf.readResourceLocation());
        this.motionType = buf.readInt();
        this.data = buf.readInt();
        this.count = buf.readInt();
        this.xPos = buf.readDouble();
        this.yPos = buf.readDouble();
        this.zPos = buf.readDouble();
        this.radiusX = buf.readDouble();
        this.radiusY = buf.readDouble();
        this.radiusZ = buf.readDouble();
        this.speed = buf.readDouble();
        this.headingX = buf.readDouble();
        this.headingY = buf.readDouble();
        this.headingZ = buf.readDouble();
        this.hasRaw = buf.readBoolean();
        if (hasRaw){
            this.anim = ParticleAnimation.deserializeFromDynamic(RAW_EFFECT, new Dynamic<>(NBTDynamicOps.INSTANCE, buf.readCompoundTag()));
            this.animName = RAW_EFFECT;
        } else {
            this.animName = buf.readResourceLocation();
            ParticleAnimation anim = ParticleAnimationManager.ANIMATIONS.get(animName);
            if (anim == null){
                this.anim = new ParticleAnimation();
                MKCore.LOGGER.warn("Failed to find managed particle animation {}", animName);
            } else {
                this.anim = anim;
            }
        }

    }

    public void toBytes(PacketBuffer buf) {
        buf.writeResourceLocation(particleType.getRegistryName());
        buf.writeInt(this.motionType);
        buf.writeInt(this.data);
        buf.writeInt(this.count);
        buf.writeDouble(this.xPos);
        buf.writeDouble(this.yPos);
        buf.writeDouble(this.zPos);
        buf.writeDouble(this.radiusX);
        buf.writeDouble(this.radiusY);
        buf.writeDouble(this.radiusZ);
        buf.writeDouble(this.speed);
        buf.writeDouble(this.headingX);
        buf.writeDouble(this.headingY);
        buf.writeDouble(this.headingZ);
        buf.writeBoolean(hasRaw);
        if (hasRaw){
            INBT dyn = anim.serialize(NBTDynamicOps.INSTANCE);
            if (dyn instanceof CompoundNBT) {
                buf.writeCompoundTag((CompoundNBT) dyn);
            } else {
                throw new RuntimeException(String.format("Particle Animation %s did not serialize to a CompoundNBT!", particleType.getRegistryName().toString()));
            }
        } else {
            buf.writeResourceLocation(animName);
        }

    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null)
            return;

        ParticleEffects.spawnMKParticleEffect(
                particleType, motionType, data, speed, count,
                new Vector3d(xPos, yPos, zPos),
                new Vector3d(radiusX, radiusY, radiusZ),
                new Vector3d(headingX, headingY, headingZ),
                player.world, anim);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(this::handleClient);
        ctx.setPacketHandled(true);
    }
}
