package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.network.MKParticleEffectSpawnPacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.utils.MKNBTUtil;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKParticleEffectNew extends MKEffect {
    public static final MKParticleEffectNew INSTANCE = new MKParticleEffectNew();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKEffect> event) {
        event.getRegistry().register(INSTANCE);
    }

    public MKParticleEffectNew() {
        super(EffectType.NEUTRAL);
        setRegistryName("effect.v2.mkparticle");
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
        public boolean performEffect(IMKEntityData entityData, MKActiveEffect instance) {
            PacketHandler.sendToTrackingAndSelf(createPacket(entityData.getEntity()), entityData.getEntity());
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
}
