package com.chaosbuffalo.mkcore.effects.utility;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkcore.network.MKParticleEffectSpawnPacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.utils.MKNBTUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

public class MKParticleEffect extends MKEffect {
    public static final MKParticleEffect INSTANCE = new MKParticleEffect();

    public MKParticleEffect() {
        super(EffectType.NEUTRAL);
        setRegistryName("effect.mk_particle");
    }

    public static MKEffectBuilder<?> from(LivingEntity source, ResourceLocation animName, boolean includeSelf, Vector3d location) {
        return INSTANCE.builder(source).state(s -> s.setup(animName, includeSelf, location));
    }

    @Override
    public State makeState() {
        return new State();
    }

    @Override
    public MKEffectBuilder<State> builder(UUID sourceId) {
        return new MKEffectBuilder<>(this, sourceId, this::makeState);
    }

    @Override
    public MKEffectBuilder<State> builder(LivingEntity sourceEntity) {
        return new MKEffectBuilder<>(this, sourceEntity, this::makeState);
    }

    public static class State extends MKEffectState {
        public ResourceLocation animName;
        public Vector3d location;
        public boolean includeSelf;

        public void setup(ResourceLocation animName, boolean includeSelf, Vector3d location) {
            this.animName = animName;
            this.includeSelf = includeSelf;
            this.location = location;
        }

        @Override
        public boolean validateOnApply(IMKEntityData targetData, MKActiveEffect activeEffect) {
            // Don't apply if we're the caster and the caller didn't want us included
            return includeSelf || !isEffectSource(targetData.getEntity(), activeEffect);
        }

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect instance) {
            PacketHandler.sendToTrackingAndSelf(createPacket(targetData.getEntity()), targetData.getEntity());
            return true;
        }

        private MKParticleEffectSpawnPacket createPacket(Entity target) {
            return new MKParticleEffectSpawnPacket(location, animName, target.getEntityId());
        }

        @Override
        public void serializeStorage(CompoundNBT stateTag) {
            super.serializeStorage(stateTag);
            stateTag.putBoolean("includeSelf", includeSelf);
            MKNBTUtil.writeVector3d(stateTag, "location", location);
            MKNBTUtil.writeResourceLocation(stateTag, "animName", animName);
        }

        @Override
        public void deserializeStorage(CompoundNBT tag) {
            super.deserializeStorage(tag);
            includeSelf = tag.getBoolean("includeSelf");
            location = MKNBTUtil.readVector3(tag, "location");
            animName = MKNBTUtil.readResourceLocation(tag, "animName");
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
