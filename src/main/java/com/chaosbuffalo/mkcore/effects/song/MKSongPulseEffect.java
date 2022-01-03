package com.chaosbuffalo.mkcore.effects.song;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import net.minecraft.potion.EffectType;

import java.util.UUID;

public abstract class MKSongPulseEffect extends MKEffect {
    public MKSongPulseEffect() {
        super(EffectType.NEUTRAL);
    }

    @Override
    public SongPulseState makeState() {
        return new SongPulseState();
    }

    @Override
    public MKEffectBuilder<SongPulseState> builder(UUID sourceId) {
        return new MKEffectBuilder<>(this, sourceId, this::makeState);
    }

    public static class SongPulseState extends MKSongStateBase {

        @Override
        public boolean performEffect(IMKEntityData entityData, MKActiveEffect instance) {
            MKCore.LOGGER.info("MKSongPulseEffect.performEffect {} {}", instance, getSongAbility());

            AreaEffectBuilder area = AreaEffectBuilder.createOnCaster(entityData.getEntity());
            getSongAbility().addPulseAreaEffects(entityData, area);

            area.instant()
                    .particle(getSongAbility().getSongPulseParticle())
                    .color(16762905)
                    .radius(getSongAbility().getSongDistance(entityData, instance), true)
                    .spawn();

            return true;
        }
    }
}
