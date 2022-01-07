package com.chaosbuffalo.mkcore.effects.utility;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.*;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import net.minecraft.entity.Entity;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SoundEffect extends MKEffect {
    public static final SoundEffect INSTANCE = new SoundEffect();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKEffect> event) {
        event.getRegistry().register(INSTANCE);
    }

    public SoundEffect() {
        super(EffectType.NEUTRAL);
        setRegistryName("effect.sound_effect");
    }

    public static MKEffectBuilder<?> from(Entity source, SoundEvent event, float pitch, float volume,
                                          SoundCategory cat) {
        return INSTANCE.builder(source.getUniqueID()).state(s -> s.setup(event, pitch, volume, cat));
    }

    public static MKEffectBuilder<?> from(Entity source, SoundEvent event, SoundCategory cat) {
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

    public static class State extends MKEffectState {
        public ResourceLocation soundEvent;
        public float volume;
        public float pitch;
        public SoundCategory category;

        public void setup(SoundEvent event, float pitch, float volume, SoundCategory cat) {
            soundEvent = event.getRegistryName();
            this.volume = volume;
            this.pitch = pitch;
            this.category = cat;
        }

        public void setup(SoundEvent event, SoundCategory cat) {
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
}
