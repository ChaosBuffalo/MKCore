package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticleData;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.mojang.serialization.Codec;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(MKCore.MOD_ID)
@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CoreParticles {

    @ObjectHolder("magic_cross")
    public static ParticleType<MKParticleData> MAGIC_CROSS;
    @ObjectHolder("magic_clover")
    public static ParticleType<MKParticleData> MAGIC_CLOVER;
    @ObjectHolder("magic_line")
    public static ParticleType<MKParticleData> MAGIC_LINE;
    @ObjectHolder("magic_circle")
    public static ParticleType<MKParticleData> MAGIC_CIRCLE;
    @ObjectHolder("magic_gradient_square")
    public static ParticleType<MKParticleData> MAGIC_GRADIENT_SQUARE;

    @SubscribeEvent
    public static void registerParticles(RegistryEvent.Register<ParticleType<?>> evt){
        ParticleType<MKParticleData> magicCross = new ParticleType<MKParticleData>(false, MKParticleData.DESERIALIZER){
            @Override
            public Codec<MKParticleData> func_230522_e_() {
                return MKParticleData.typeCodec(this);
            }
        };
        magicCross.setRegistryName(CoreRegistryNames.MAGIC_CROSS_NAME);
        evt.getRegistry().register(magicCross);

        ParticleType<MKParticleData> magicClover = new ParticleType<MKParticleData>(false, MKParticleData.DESERIALIZER){
            @Override
            public Codec<MKParticleData> func_230522_e_() {
                return MKParticleData.typeCodec(this);
            }
        };
        magicClover.setRegistryName(CoreRegistryNames.MAGIC_CLOVER_NAME);
        evt.getRegistry().register(magicClover);

        ParticleType<MKParticleData> magicLine = new ParticleType<MKParticleData>(false, MKParticleData.DESERIALIZER){
            @Override
            public Codec<MKParticleData> func_230522_e_() {
                return MKParticleData.typeCodec(this);
            }
        };
        magicLine.setRegistryName(CoreRegistryNames.MAGIC_LINE_NAME);
        evt.getRegistry().register(magicLine);

        ParticleType<MKParticleData> magicCircle = new ParticleType<MKParticleData>(false, MKParticleData.DESERIALIZER){
            @Override
            public Codec<MKParticleData> func_230522_e_() {
                return MKParticleData.typeCodec(this);
            }
        };
        magicCircle.setRegistryName(CoreRegistryNames.MAGIC_CIRCLE_NAME);
        evt.getRegistry().register(magicCircle);

        ParticleType<MKParticleData> magicGradientSquare = new ParticleType<MKParticleData>(false, MKParticleData.DESERIALIZER){
            @Override
            public Codec<MKParticleData> func_230522_e_() {
                return MKParticleData.typeCodec(this);
            }
        };
        magicGradientSquare.setRegistryName(CoreRegistryNames.MAGIC_GRADIENT_SQUARE_NAME);
        evt.getRegistry().register(magicGradientSquare);
    }

    public static void HandleEditorParticleRegistration(){
        ParticleAnimationManager.putParticleTypeForEditor(CoreRegistryNames.MAGIC_LINE_NAME, MAGIC_LINE);
        ParticleAnimationManager.putParticleTypeForEditor(CoreRegistryNames.MAGIC_CIRCLE_NAME, MAGIC_CIRCLE);
        ParticleAnimationManager.putParticleTypeForEditor(CoreRegistryNames.MAGIC_CROSS_NAME, MAGIC_CROSS);
        ParticleAnimationManager.putParticleTypeForEditor(CoreRegistryNames.MAGIC_GRADIENT_SQUARE_NAME, MAGIC_GRADIENT_SQUARE);
        ParticleAnimationManager.putParticleTypeForEditor(CoreRegistryNames.MAGIC_CLOVER_NAME, MAGIC_CLOVER);
    }

}
