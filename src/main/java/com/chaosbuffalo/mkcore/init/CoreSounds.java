package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
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
    @ObjectHolder("block_break")
    public static SoundEvent block_break;
    @ObjectHolder("weapon_block")
    public static SoundEvent weapon_block;
    @ObjectHolder("arrow_block")
    public static SoundEvent arrow_block;
    @ObjectHolder("fist_block")
    public static SoundEvent fist_block;
    @ObjectHolder("parry")
    public static SoundEvent parry;
    @ObjectHolder("attack_cd_reset")
    public static SoundEvent attack_cd_reset;
    @ObjectHolder("stun")
    public static SoundEvent stun_sound;
    @ObjectHolder("quest_complete")
    public static SoundEvent quest_complete_sound;

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
        registerSound(evt, createSound("block_break"));
        registerSound(evt, createSound("arrow_block"));
        registerSound(evt, createSound("fist_block"));
        registerSound(evt, createSound("weapon_block"));
        registerSound(evt, createSound("parry"));
        registerSound(evt, createSound("attack_cd_reset"));
        registerSound(evt, createSound("stun"));
        registerSound(evt, createSound("quest_complete"));
    }
}
