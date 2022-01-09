package com.chaosbuffalo.mkcore.client.rendering;

import com.chaosbuffalo.mkcore.MKConfig;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.entities.MKAreaEffectEntity;
import com.chaosbuffalo.mkcore.fx.particles.IndicatorParticle;
import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import com.chaosbuffalo.mkcore.init.CoreParticles;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKRenderers {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent evt) {
        RenderingRegistry.registerEntityRenderingHandler(MKAreaEffectEntity.TYPE, EntityMKAreaEffectRenderer::new);
    }

    public static void registerPlayerRenderers() {
        if (MKConfig.CLIENT.enablePlayerCastAnimations.get()) {
            Minecraft.getInstance().getRenderManager().skinMap.put("default",
                    new MKPlayerRenderer(Minecraft.getInstance().getRenderManager(), false));
            Minecraft.getInstance().getRenderManager().skinMap.put("slim",
                    new MKPlayerRenderer(Minecraft.getInstance().getRenderManager(), true));
        }
    }


    @SubscribeEvent
    public static void registerParticleFactory(ParticleFactoryRegisterEvent evt){
        Minecraft.getInstance().particles.registerFactory(CoreParticles.MAGIC_CROSS,
                (spriteSet) -> new MKParticle.MKParticleFactory(
                        spriteSet, -0.0001f, 0.05f,
                        0.05f, 80, true,
                        null));
        Minecraft.getInstance().particles.registerFactory(CoreParticles.MAGIC_CLOVER,
                (spriteSet) -> new MKParticle.MKParticleFactory(
                        spriteSet, -0.0001f, 0.05f,
                        0.05f, 80, true,
                        null));
        Minecraft.getInstance().particles.registerFactory(CoreParticles.MAGIC_LINE,
                (spriteSet) -> new MKParticle.MKParticleFactory(
                        spriteSet, -0.0001f, 0.05f,
                        0.05f, 80, true,
                        null));
        Minecraft.getInstance().particles.registerFactory(CoreParticles.MAGIC_CIRCLE,
                (spriteSet) -> new MKParticle.MKParticleFactory(
                        spriteSet, -0.0001f, 0.05f,
                        0.05f, 80, true,
                        null));
        Minecraft.getInstance().particles.registerFactory(CoreParticles.MAGIC_GRADIENT_SQUARE,
                (spriteSet) -> new MKParticle.MKParticleFactory(
                        spriteSet, -0.0001f, 0.05f,
                        0.05f, 80, true,
                        null));
        Minecraft.getInstance().particles.registerFactory(CoreParticles.INDICATOR_PARTICLE,
                IndicatorParticle.IndicatorFactory::new);
    }
}
