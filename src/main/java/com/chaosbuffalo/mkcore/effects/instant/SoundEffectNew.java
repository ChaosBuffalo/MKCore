package com.chaosbuffalo.mkcore.effects.instant;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.*;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
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
public class SoundEffectNew extends MKEffect {
    public static final SoundEffectNew INSTANCE = new SoundEffectNew();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKEffect> event) {
        event.getRegistry().register(INSTANCE);
    }

    public SoundEffectNew() {
        super(EffectType.NEUTRAL);
        setRegistryName(MKCore.makeRL("effect.v2.sound_effect"));
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
