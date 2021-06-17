package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import com.chaosbuffalo.mkcore.fx.particles.MKParticleData;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(MKCore.MOD_ID)
@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CoreParticles {

    @ObjectHolder("blue_magic_cross")
    public static ParticleType<MKParticleData> BLUE_MAGIC_CROSS;

    @SubscribeEvent
    public static void registerParticles(RegistryEvent.Register<ParticleType<?>> evt){
        ParticleType<MKParticleData> blueMagicCross = new ParticleType<MKParticleData>(false, MKParticleData.DESERIALIZER){
            @Override
            public Codec<MKParticleData> func_230522_e_() {
                return MKParticleData.typeCodec(this);
            }
        };
        blueMagicCross.setRegistryName(MKCore.MOD_ID, "blue_magic_cross");
        evt.getRegistry().register(blueMagicCross);
    }

}
