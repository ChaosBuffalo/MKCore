package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.mojang.serialization.Dynamic;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MKParticleEffectSpawnPacket {
    protected final double xPos;
    protected final double yPos;
    protected final double zPos;
    protected final ParticleAnimation anim;
    protected final ResourceLocation animName;
    protected final boolean hasRaw;
    protected final int entityId;
    protected final List<Vector3d> additionalLocs;


    public MKParticleEffectSpawnPacket(double xPos, double yPos, double zPos, ParticleAnimation anim, int entityId) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
        this.anim = anim;
        this.hasRaw = true;
        this.animName = ParticleAnimationManager.RAW_EFFECT;
        this.entityId = entityId;
        this.additionalLocs = new ArrayList<>();
    }

    public MKParticleEffectSpawnPacket(double xPos, double yPos, double zPos, ParticleAnimation anim){
        this(xPos, yPos, zPos, anim, -1);
    }

    public MKParticleEffectSpawnPacket(double xPos, double yPos, double zPos, ResourceLocation animName, int entityId) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
        this.anim = null;
        this.animName = animName;
        this.hasRaw = false;
        this.entityId = entityId;
        this.additionalLocs = new ArrayList<>();
    }

    public void addLoc(Vector3d loc){
        additionalLocs.add(loc);
    }

    public MKParticleEffectSpawnPacket(double xPos, double yPos, double zPos, ResourceLocation animName) {
        this(xPos, yPos, zPos, animName, -1);
    }


    public MKParticleEffectSpawnPacket(Vector3d posVec, ParticleAnimation anim) {
        this(posVec.x, posVec.y, posVec.z, anim);
    }

    public MKParticleEffectSpawnPacket(Vector3d posVec, ResourceLocation animName, int entityId){
        this(posVec.getX(), posVec.getY(), posVec.getZ(), animName, entityId);
    }

    public MKParticleEffectSpawnPacket(Vector3d posVec, ResourceLocation animName){
        this(posVec, animName, -1);
    }

    public MKParticleEffectSpawnPacket(PacketBuffer buf) {
        this.xPos = buf.readDouble();
        this.yPos = buf.readDouble();
        this.zPos = buf.readDouble();
        this.entityId = buf.readInt();
        int addVecCount = buf.readInt();
        additionalLocs = new ArrayList<>();
        for (int i = 0; i < addVecCount; i++){
            additionalLocs.add(new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble()));
        }
        this.hasRaw = buf.readBoolean();
        if (hasRaw){
            this.anim = ParticleAnimation.deserializeFromDynamic(ParticleAnimationManager.RAW_EFFECT,
                    new Dynamic<>(NBTDynamicOps.INSTANCE, buf.readCompoundTag()));
            this.animName = ParticleAnimationManager.RAW_EFFECT;
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
        buf.writeDouble(this.xPos);
        buf.writeDouble(this.yPos);
        buf.writeDouble(this.zPos);
        buf.writeInt(this.entityId);
        buf.writeInt(additionalLocs.size());
        for (Vector3d vec : additionalLocs){
            buf.writeDouble(vec.getX());
            buf.writeDouble(vec.getY());
            buf.writeDouble(vec.getZ());
        }
        buf.writeBoolean(hasRaw);
        if (hasRaw){
            INBT dyn = anim.serialize(NBTDynamicOps.INSTANCE);
            if (dyn instanceof CompoundNBT) {
                buf.writeCompoundTag((CompoundNBT) dyn);
            } else {
                throw new RuntimeException(String.format("Particle Animation %s did not serialize to a CompoundNBT!", anim.toString()));
            }
        } else {
            buf.writeResourceLocation(animName);
        }

    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null || anim == null)
            return;
        if (entityId != -1){
            Entity source = player.getEntityWorld().getEntityByID(entityId);
            if (source != null){
                anim.spawnOffsetFromEntity(player.getEntityWorld(), new Vector3d(xPos, yPos, zPos),
                        source, additionalLocs);
            }
        } else {
            anim.spawn(player.getEntityWorld(), new Vector3d(xPos, yPos, zPos), additionalLocs);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(this::handleClient);
        ctx.setPacketHandled(true);
    }
}
