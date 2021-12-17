package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(MKCore.MOD_ID)
public class CoreSounds {

    @ObjectHolder("casting_default")
    public static SoundEvent casting_default;
    @ObjectHolder("spell_cast_default")
    public static SoundEvent spell_cast_default;
    @ObjectHolder("level_up")
    public static SoundEvent level_up;

    public static void registerSound(RegistryEvent.Register<SoundEvent> evt, SoundEvent event) {
        evt.getRegistry().register(event);
    }

    public static SoundEvent createSound(String name) {
        ResourceLocation r_name = MKCore.makeRL(name);
        SoundEvent event = new SoundEvent(r_name);
        event.setRegistryName(r_name);
        return event;
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> evt) {
        registerSound(evt, createSound("casting_default"));
        registerSound(evt, createSound("spell_cast_default"));
        registerSound(evt, createSound("level_up"));
    }
}
