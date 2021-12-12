package com.chaosbuffalo.mkcore.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;

public class SoundUtils {
    public static void playSoundAtEntity(Entity entity, SoundEvent event) {
        playSoundAtEntity(entity, event, entity.getSoundCategory(), 1.0f, 1.0f);
    }

    public static void playSoundAtEntity(Entity entity, SoundEvent event, SoundCategory cat) {
        playSoundAtEntity(entity, event, cat, 1.0f, 1.0f);
    }

    public static void playSoundAtEntity(Entity entity, SoundEvent event, SoundCategory cat, float volume) {
        playSoundAtEntity(entity, event, cat, volume, 1.0F);
    }

    public static void playSoundAtEntity(Entity entity, SoundEvent event, SoundCategory cat, float volume, float pitch) {
        if (event == null) {
            return;
        }
        entity.world.playSound(null, entity.getPosX(), entity.getPosY(), entity.getPosZ(), event, cat, volume, pitch);
    }

    public static void serverPlaySoundFromEntity(double x, double y, double z,
                                               SoundEvent soundIn, SoundCategory category, float volume, float pitch,
                                               Entity source){
        net.minecraftforge.event.entity.PlaySoundAtEntityEvent event = net.minecraftforge.event.ForgeEventFactory
                .onPlaySoundAtEntity(source, soundIn, category, volume, pitch);
        if (event.isCanceled() || event.getSound() == null) return;
        soundIn = event.getSound();
        category = event.getCategory();
        volume = event.getVolume();
        PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> source).send(
                new SPlaySoundEffectPacket(soundIn, category, x, y, z, volume, pitch));
    }

    public static void serverPlaySoundAtEntity(Entity source, SoundEvent soundIn, SoundCategory category, float volume, float pitch){
        serverPlaySoundFromEntity(source.getPosX(), source.getPosY(), source.getPosZ(), soundIn, category,
                volume, pitch, source);
    }

    public static void serverPlaySoundAtEntity(Entity source, SoundEvent soundIn, SoundCategory category){
        serverPlaySoundFromEntity(source.getPosX(), source.getPosY(), source.getPosZ(), soundIn, category,
                1.0f, 1.0f, source);
    }
}
