package com.chaosbuffalo.mkcore.test.v2;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.potion.EffectType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NewParticleEffect extends MKEffect {

    public static final NewParticleEffect INSTANCE = new NewParticleEffect();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKEffect> event) {
        event.getRegistry().register(INSTANCE);
    }

    public static MKEffectBuilder<State> Create(Entity source, IParticleData particleId, int motionType, boolean includeSelf,
                                                Vector3d radius, Vector3d offsets, int particleCount, int particleData,
                                                double particleSpeed) {
        MKEffectBuilder<State> builder = INSTANCE.builder(source.getUniqueID());
        builder.state(s ->
                s.setup(source, particleId, motionType, includeSelf, radius, offsets, particleCount, particleData, particleSpeed));
        return builder;
    }

    protected NewParticleEffect() {
        super(EffectType.NEUTRAL);
        setRegistryName("effect.particle");
    }

    @Override
    public MKEffectBuilder<State> builder(UUID sourceId) {
        return new MKEffectBuilder<>(this, sourceId, this::makeState);
    }

    @Override
    public State makeState() {
        return new State();
    }

    public static class State extends MKEffectState {
        private Entity source;
        private IParticleData particleId;
        private int motionType;
        private Vector3d radius;
        private Vector3d offsets;
        private int particleCount;
        private int particleData;
        private double particleSpeed;
        private boolean includeSelf;

        public void setup(Entity source, IParticleData particleId, int motionType, boolean includeSelf,
                          Vector3d radius, Vector3d offsets, int particleCount, int particleData,
                          double particleSpeed) {
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

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect instance) {
            LivingEntity target = targetData.getEntity();
            MKCore.LOGGER.info("NewParticleEffect.Instance.performEffect {}", target);
            if (!includeSelf && isEffectSource(target, instance)) {
                return false;
            }
            PacketHandler.sendToTrackingAndSelf(createPacket(target), target);
            return true;
        }
    }
}