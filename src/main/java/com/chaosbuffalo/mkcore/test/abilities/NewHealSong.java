package com.chaosbuffalo.mkcore.test.abilities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKSongAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.song.MKSongPulseEffect;
import com.chaosbuffalo.mkcore.effects.song.MKSongSustainEffect;
import com.chaosbuffalo.mkcore.test.effects.NewHealEffect;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class NewHealSong extends MKSongAbility {
    public NewHealSong() {
        super();
    }

    @Override
    public NewHealSongSustainEffect getSustainEffect() {
        return NewHealSongSustainEffect.INSTANCE;
    }

    @Override
    public int getSustainEffectTicks() {
        return 18 * GameConstants.TICKS_PER_SECOND;
    }

    @Override
    public NewHealSongPulseEffect getPulseEffect() {
        return NewHealSongPulseEffect.INSTANCE;
    }

    @Override
    public int getPulseEffectTicks() {
        return 6 * GameConstants.TICKS_PER_SECOND;
    }

    @Override
    public void addPulseAreaEffects(IMKEntityData casterData, AreaEffectBuilder areaEffect) {
        MKEffectBuilder<?> effect = NewHealEffect.INSTANCE.builder(casterData.getEntity())
                .state(s -> s.setScalingParameters(3, 1))
                .ability(this);

        areaEffect.effect(effect, TargetingContexts.FRIENDLY);
    }

    public static class NewHealSongSustainEffect extends MKSongSustainEffect {
        public static final NewHealSongSustainEffect INSTANCE = new NewHealSongSustainEffect();

        public NewHealSongSustainEffect() {
            super();
            setRegistryName(MKCore.makeRL("effect.v2.new_heal_song_sustain"));
        }
    }

    public static class NewHealSongPulseEffect extends MKSongPulseEffect {
        public static final NewHealSongPulseEffect INSTANCE = new NewHealSongPulseEffect();

        public NewHealSongPulseEffect() {
            setRegistryName(MKCore.makeRL("effect.v2.new_heal_song_pulse"));
        }
    }

    @SuppressWarnings("unused")
    @Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    private static class RegisterMe {

        @SubscribeEvent
        public static void registerEffects(RegistryEvent.Register<MKEffect> event) {
            event.getRegistry().register(NewHealSongSustainEffect.INSTANCE);
            event.getRegistry().register(NewHealSongPulseEffect.INSTANCE);
        }
    }
}
