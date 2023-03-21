package com.chaosbuffalo.mkcore.effects.utility;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

public class SoundEffect extends MKEffect {
    public static final SoundEffect INSTANCE = new SoundEffect();

    public SoundEffect() {
        super(MobEffectCategory.NEUTRAL);
        setRegistryName("effect.sound_effect");
    }

    public static MKEffectBuilder<?> from(LivingEntity source, SoundEvent event, float pitch, float volume,
                                          SoundSource cat) {
        return INSTANCE.builder(source).state(s -> s.setup(event, pitch, volume, cat));
    }

    public static MKEffectBuilder<?> from(LivingEntity source, SoundEvent event, SoundSource cat) {
        return from(source, event, 1f, 1f, cat);
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
        public ResourceLocation soundEvent;
        public float volume;
        public float pitch;
        public SoundSource category;

        public void setup(SoundEvent event, float pitch, float volume, SoundSource cat) {
            soundEvent = event.getRegistryName();
            this.volume = volume;
            this.pitch = pitch;
            this.category = cat;
        }

        public void setup(SoundEvent event, SoundSource cat) {
            setup(event, 1f, 1f, cat);
        }

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect instance) {
            SoundEvent event = ForgeRegistries.SOUND_EVENTS.getValue(soundEvent);
            if (event == null)
                return false;

            if (targetData.isServerSide()) {
                SoundUtils.serverPlaySoundAtEntity(targetData.getEntity(), event, category, volume, pitch);
            }
            return true;
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
