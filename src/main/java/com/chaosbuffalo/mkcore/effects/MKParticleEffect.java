package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.network.MKParticleEffectSpawnPacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKParticleEffect extends SpellEffectBase {

    public static final MKParticleEffect INSTANCE = new MKParticleEffect();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Effect> event) {
        event.getRegistry().register(INSTANCE);
    }

    public static SpellCast Create(Entity source, ResourceLocation animName, boolean includeSelf, Vector3d location) {
        return new MKParticleCast(source, animName, includeSelf, location);
    }

    protected MKParticleEffect() {
        super(EffectType.NEUTRAL, 123);
        setRegistryName("effect.mkparticle_potion");
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ALL;
    }

    @Override
    public void doEffect(Entity applier, Entity caster,
                         LivingEntity target, int amplifier, SpellCast cast) {
        if (!(cast instanceof MKParticleCast)) {
            MKCore.LOGGER.error("Got to MKParticlePotion.doEffect with a cast that wasn't a MKParticleCast!");
            return;
        }
        MKParticleCast particleCast = (MKParticleCast) cast;
        // Check canSelfCast first
        if (!particleCast.includeSelf && target.equals(caster)) {
            return;
        }
        PacketHandler.sendToTrackingAndSelf(particleCast.createPacket(target), target);
    }

    public static class MKParticleCast extends SpellCast {
        Entity source;
        ResourceLocation animName;
        Vector3d location;
        boolean includeSelf;

        public MKParticleCast(Entity source, ResourceLocation animName, boolean includeSelf, Vector3d location) {
            super(MKParticleEffect.INSTANCE, source);
            this.source = source;
            this.animName = animName;
            this.includeSelf = includeSelf;
            this.location = location;
        }

        public MKParticleEffectSpawnPacket createPacket(Entity target) {
            return new MKParticleEffectSpawnPacket(location, animName, target.getEntityId());
        }
    }
}
