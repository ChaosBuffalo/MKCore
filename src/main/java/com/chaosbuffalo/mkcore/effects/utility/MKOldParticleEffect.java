package com.chaosbuffalo.mkcore.effects.utility;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.*;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import net.minecraft.entity.Entity;
import net.minecraft.particles.IParticleData;
import net.minecraft.potion.EffectType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

public class MKOldParticleEffect extends MKEffect {
    public static final MKOldParticleEffect INSTANCE = new MKOldParticleEffect();

    public MKOldParticleEffect() {
        super(EffectType.NEUTRAL);
        setRegistryName("effect.old_particle");
    }

    public static MKEffectBuilder<?> from(Entity source, IParticleData particleId, int motionType, boolean includeSelf,
                                          Vector3d radius, Vector3d offsets, int particleCount, int particleData,
                                          double particleSpeed) {
        return INSTANCE.builder(source.getUniqueID()).state(s ->
                s.setup(source, particleId, motionType, radius, offsets, particleCount, particleData, particleSpeed, includeSelf));
    }

    @Override
    public State makeState() {
        return new State();
    }

    @Override
    public MKEffectBuilder<State> builder(UUID sourceId) {
        return new MKEffectBuilder<>(this, sourceId, this::makeState);
    }

    public static class State extends MKEffectState {
        Entity source;
        IParticleData particleId;
        int motionType;
        Vector3d radius;
        Vector3d offsets;
        int particleCount;
        int particleData;
        double particleSpeed;
        boolean includeSelf;

        public void setup(Entity source, IParticleData particleId, int motionType,
                            Vector3d radius, Vector3d offsets, int particleCount, int particleData,
                            double particleSpeed, boolean includeSelf) {
            this.source = source;
            this.particleId = particleId;
            this.motionType = motionType;
            this.radius = radius;
            this.offsets = offsets;
            this.particleCount = particleCount;
            this.particleData = particleData;
            this.particleSpeed = particleSpeed;
            this.includeSelf = includeSelf;
        }

        @Override
        public boolean validateOnApply(IMKEntityData targetData, MKActiveEffect activeEffect) {
            // Don't apply if we're the caster and the caller didn't want us included
            return includeSelf || !isEffectSource(targetData.getEntity(), activeEffect);
        }

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect instance) {
            PacketHandler.sendToTrackingAndSelf(createPacket(targetData.getEntity()), targetData.getEntity());
            return false;
        }

        public ParticleEffectSpawnPacket createPacket(Entity target) {
            return new ParticleEffectSpawnPacket(particleId,
                    motionType, particleCount, particleData,
                    target.getPosX() + offsets.x,
                    target.getPosY() + offsets.y,
                    target.getPosZ() + offsets.z,
                    radius.x,
                    radius.y,
                    radius.z,
                    particleSpeed,
                    source.getPositionVec().subtract(target.getPositionVec()).normalize());
        }
    }

    @SuppressWarnings("unused")
    @Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    private static class RegisterMe {
        @SubscribeEvent
        public static void register(RegistryEvent.Register<MKEffect> event) {
            event.getRegistry().register(INSTANCE);
        }
    }
}
