package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.song.MKSongPulseEffect;
import com.chaosbuffalo.mkcore.effects.song.MKSongSustainEffect;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;

public abstract class MKSongAbilityNew extends MKToggleAbilityNew {
    public MKSongAbilityNew(ResourceLocation abilityId) {
        super(abilityId);
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.SELF;
    }

    @Override
    public final MKSongSustainEffect getToggleEffect() {
        return getSustainEffect();
    }

    public abstract MKSongSustainEffect getSustainEffect();

    public abstract int getSustainEffectTicks();

    public abstract MKSongPulseEffect getPulseEffect();

    public abstract int getPulseEffectTicks();

    @Override
    public void applyEffect(LivingEntity entity, IMKEntityData entityData) {
        super.applyEffect(entity, entityData);
        applySustainEffect(entityData);
    }

    public MKActiveEffect createSustainEffect(IMKEntityData casterData) {
        return getSustainEffect().builder(casterData.getEntity().getUniqueID())
                .ability(this)
                .periodic(getSustainEffectTicks())
                .infinite()
                .createApplication();
    }

    public MKActiveEffect createPulseEffect(IMKEntityData casterData) {
        return getPulseEffect().builder(casterData.getEntity().getUniqueID())
                .ability(this)
                .periodic(getPulseEffectTicks())
                .timed(getSustainEffectTicks())
                .createApplication();
    }

    public float getSongDistance(IMKEntityData entityData, MKActiveEffect instance) {
        return 10f;
    }

    public IParticleData getSongPulseParticle() {
        return ParticleTypes.NOTE;
    }

    protected void applySustainEffect(IMKEntityData casterData) {
        MKCore.LOGGER.info("MKSongAbilityNew.applySustainEffect");

        MKActiveEffect sustain = createSustainEffect(casterData);
        casterData.getEffects().addEffect(sustain);

        LivingEntity entity = casterData.getEntity();
        PacketHandler.sendToTrackingAndSelf(new ParticleEffectSpawnPacket(
                ParticleTypes.NOTE,
                ParticleEffects.SPHERE_MOTION, 50, 5,
                entity.getPosX(), entity.getPosY() + 1.0,
                entity.getPosZ(), 1.0, 1.0, 1.0, 1.0f,
                entity.getLookVec()), entity);
    }

    public void addPulseAreaEffects(IMKEntityData casterData, AreaEffectBuilder addEffect) {

    }

    public int getSustainEffectManaCost(IMKEntityData entityData, MKActiveEffect activeEffect) {
        return 1;
    }

    @Override
    public float getManaCost(IMKEntityData entityData) {
        // Songs cost nothing to activate, but the CasterEffect will try to drain getSustainEffectManaCost() on the first tick
        return 0;
    }
}
