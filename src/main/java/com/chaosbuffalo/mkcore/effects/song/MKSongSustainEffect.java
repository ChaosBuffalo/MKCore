package com.chaosbuffalo.mkcore.effects.song;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectType;

import java.util.UUID;

public abstract class MKSongSustainEffect extends MKEffect {

    public MKSongSustainEffect() {
        super(EffectType.BENEFICIAL);
    }

    @Override
    public SongSustainState makeState() {
        return new SongSustainState();
    }

    @Override
    public MKEffectBuilder<SongSustainState> builder(UUID sourceId) {
        return new MKEffectBuilder<>(this, sourceId, this::makeState);
    }

    public static class SongSustainState extends MKSongStateBase {

        @Override
        public boolean performEffect(IMKEntityData entityData, MKActiveEffect instance) {
            MKCore.LOGGER.info("MKSongSustainEffect.performEffect {} {}", instance, getSongAbility());

//            if (entityData.getAbilityExecutor().isCasting() ||
//                    !entityData.getStats().consumeMana(ability.getSustainEffectManaCost(playerData))) {
//                entity.removePotionEffect(this);
//                return;
//            }

            if (entityData instanceof MKPlayerData) {
                MKPlayerData playerData = (MKPlayerData) entityData;
                if (!playerData.getStats().consumeMana(getSongAbility().getSustainEffectManaCost(playerData, instance))) {
                    // Remove the effect if you can't pay the upkeep
                    return false;
                }
            }

            MKActiveEffect pulse = getSongAbility().createPulseEffect(entityData);
            entityData.getEffects().addEffect(pulse);

            LivingEntity target = entityData.getEntity();
            PacketHandler.sendToTrackingAndSelf(new ParticleEffectSpawnPacket(
                    ParticleTypes.NOTE,
                    ParticleEffects.CIRCLE_MOTION, 12, 4,
                    target.getPosX(), target.getPosY() + 1.0f,
                    target.getPosZ(), .25, .25, .25, .5,
                    target.getLookVec()), target);
            return true;
        }
    }
}