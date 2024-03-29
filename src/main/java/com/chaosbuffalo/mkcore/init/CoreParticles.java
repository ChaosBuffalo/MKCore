package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.IndicatorParticle;
import com.chaosbuffalo.mkcore.fx.particles.MKParticleData;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
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
    @ObjectHolder("indicator_particle")
    public static BasicParticleType INDICATOR_PARTICLE;
    @ObjectHolder("magic_sideways_line")
    public static ParticleType<MKParticleData> MAGIC_SIDEWAYS_LINE;
    @ObjectHolder("magic_chip")
    public static ParticleType<MKParticleData> MAGIC_CHIP;
    @ObjectHolder("black_magic_cross")
    public static ParticleType<MKParticleData> BLACK_MAGIC_CROSS;
    @ObjectHolder("black_magic_clover")
    public static ParticleType<MKParticleData> BLACK_MAGIC_CLOVER;
    @ObjectHolder("black_magic_line")
    public static ParticleType<MKParticleData> BLACK_MAGIC_LINE;
    @ObjectHolder("black_magic_circle")
    public static ParticleType<MKParticleData> BLACK_MAGIC_CIRCLE;
    @ObjectHolder("black_magic_gradient_square")
    public static ParticleType<MKParticleData> BLACK_MAGIC_GRADIENT_SQUARE;

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

        ParticleType<MKParticleData> magicSidewaysLine = new ParticleType<MKParticleData>(false, MKParticleData.DESERIALIZER){
            @Override
            public Codec<MKParticleData> func_230522_e_() {
                return MKParticleData.typeCodec(this);
            }
        };
        magicSidewaysLine.setRegistryName(CoreRegistryNames.MAGIC_SIDEWAYS_LINE_NAME);
        evt.getRegistry().register(magicSidewaysLine);

        ParticleType<MKParticleData> magicChip = new ParticleType<MKParticleData>(false, MKParticleData.DESERIALIZER){
            @Override
            public Codec<MKParticleData> func_230522_e_() {
                return MKParticleData.typeCodec(this);
            }
        };
        magicChip.setRegistryName(CoreRegistryNames.MAGIC_CHIP_NAME);
        evt.getRegistry().register(magicChip);

        BasicParticleType indiciatorParticle = new BasicParticleType(true);
        indiciatorParticle.setRegistryName(MKCore.MOD_ID, "indicator_particle");
        evt.getRegistry().register(indiciatorParticle);

        ParticleType<MKParticleData> blackMagicCross = new ParticleType<MKParticleData>(false, MKParticleData.DESERIALIZER){
            @Override
            public Codec<MKParticleData> func_230522_e_() {
                return MKParticleData.typeCodec(this);
            }
        };
        blackMagicCross.setRegistryName(CoreRegistryNames.BLACK_MAGIC_CROSS_NAME);
        evt.getRegistry().register(blackMagicCross);
        ParticleType<MKParticleData> blackMagicClover = new ParticleType<MKParticleData>(false, MKParticleData.DESERIALIZER){
            @Override
            public Codec<MKParticleData> func_230522_e_() {
                return MKParticleData.typeCodec(this);
            }
        };
        blackMagicClover.setRegistryName(CoreRegistryNames.BLACK_MAGIC_CLOVER_NAME);
        evt.getRegistry().register(blackMagicClover);
        ParticleType<MKParticleData> blackMagicLine = new ParticleType<MKParticleData>(false, MKParticleData.DESERIALIZER){
            @Override
            public Codec<MKParticleData> func_230522_e_() {
                return MKParticleData.typeCodec(this);
            }
        };
        blackMagicLine.setRegistryName(CoreRegistryNames.BLACK_MAGIC_LINE_NAME);
        evt.getRegistry().register(blackMagicLine);
        ParticleType<MKParticleData> blackMagicCircle = new ParticleType<MKParticleData>(false, MKParticleData.DESERIALIZER){
            @Override
            public Codec<MKParticleData> func_230522_e_() {
                return MKParticleData.typeCodec(this);
            }
        };
        blackMagicCircle.setRegistryName(CoreRegistryNames.BLACK_MAGIC_CIRCLE_NAME);
        evt.getRegistry().register(blackMagicCircle);
        ParticleType<MKParticleData> blackMagicGradientSquare = new ParticleType<MKParticleData>(false, MKParticleData.DESERIALIZER){
            @Override
            public Codec<MKParticleData> func_230522_e_() {
                return MKParticleData.typeCodec(this);
            }
        };
        blackMagicGradientSquare.setRegistryName(CoreRegistryNames.BLACK_MAGIC_GRADIENT_SQUARE_NAME);
        evt.getRegistry().register(blackMagicGradientSquare);


    }

    public static void HandleEditorParticleRegistration(){
        ParticleAnimationManager.putParticleTypeForEditor(CoreRegistryNames.MAGIC_LINE_NAME, MAGIC_LINE);
        ParticleAnimationManager.putParticleTypeForEditor(CoreRegistryNames.MAGIC_CIRCLE_NAME, MAGIC_CIRCLE);
        ParticleAnimationManager.putParticleTypeForEditor(CoreRegistryNames.MAGIC_CROSS_NAME, MAGIC_CROSS);
        ParticleAnimationManager.putParticleTypeForEditor(CoreRegistryNames.MAGIC_GRADIENT_SQUARE_NAME, MAGIC_GRADIENT_SQUARE);
        ParticleAnimationManager.putParticleTypeForEditor(CoreRegistryNames.MAGIC_CLOVER_NAME, MAGIC_CLOVER);
        ParticleAnimationManager.putParticleTypeForEditor(CoreRegistryNames.MAGIC_SIDEWAYS_LINE_NAME, MAGIC_SIDEWAYS_LINE);
        ParticleAnimationManager.putParticleTypeForEditor(CoreRegistryNames.MAGIC_CHIP_NAME, MAGIC_CHIP);
        ParticleAnimationManager.putParticleTypeForEditor(CoreRegistryNames.BLACK_MAGIC_CROSS_NAME, BLACK_MAGIC_CROSS);
        ParticleAnimationManager.putParticleTypeForEditor(CoreRegistryNames.BLACK_MAGIC_CLOVER_NAME, BLACK_MAGIC_CLOVER);
        ParticleAnimationManager.putParticleTypeForEditor(CoreRegistryNames.BLACK_MAGIC_LINE_NAME, BLACK_MAGIC_LINE);
        ParticleAnimationManager.putParticleTypeForEditor(CoreRegistryNames.BLACK_MAGIC_CIRCLE_NAME, BLACK_MAGIC_CIRCLE);
        ParticleAnimationManager.putParticleTypeForEditor(CoreRegistryNames.BLACK_MAGIC_GRADIENT_SQUARE_NAME, BLACK_MAGIC_GRADIENT_SQUARE);
    }

}
