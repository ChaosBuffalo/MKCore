package com.chaosbuffalo.mkcore.client.rendering;

import com.chaosbuffalo.mkcore.MKConfig;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.entities.LineEffectEntity;
import com.chaosbuffalo.mkcore.entities.MKAreaEffectEntity;
import com.chaosbuffalo.mkcore.entities.PointEffectEntity;
import com.chaosbuffalo.mkcore.fx.particles.IndicatorParticle;
import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import com.chaosbuffalo.mkcore.fx.particles.ParticleRenderTypes;
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
        RenderingRegistry.registerEntityRenderingHandler(LineEffectEntity.TYPE, BaseEffectEntityRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(PointEffectEntity.TYPE, BaseEffectEntityRenderer::new);
    }

    public static void registerPlayerRenderers() {
        if (MKConfig.CLIENT.enablePlayerCastAnimations.get()) {
            Minecraft.getInstance().getEntityRenderDispatcher().playerRenderers.put("default",
                    new MKPlayerRenderer(Minecraft.getInstance().getEntityRenderDispatcher(), false));
            Minecraft.getInstance().getEntityRenderDispatcher().playerRenderers.put("slim",
                    new MKPlayerRenderer(Minecraft.getInstance().getEntityRenderDispatcher(), true));
        }
    }


    @SubscribeEvent
    public static void registerParticleFactory(ParticleFactoryRegisterEvent evt){
        Minecraft.getInstance().particleEngine.register(CoreParticles.MAGIC_CROSS,
                (spriteSet) -> new MKParticle.MKParticleFactory(
                        spriteSet, -0.0001f, 0.05f,
                        0.05f, 80, true,
                        null));
        Minecraft.getInstance().particleEngine.register(CoreParticles.MAGIC_CLOVER,
                (spriteSet) -> new MKParticle.MKParticleFactory(
                        spriteSet, -0.0001f, 0.05f,
                        0.05f, 80, true,
                        null));
        Minecraft.getInstance().particleEngine.register(CoreParticles.MAGIC_LINE,
                (spriteSet) -> new MKParticle.MKParticleFactory(
                        spriteSet, -0.0001f, 0.05f,
                        0.05f, 80, true,
                        null));
        Minecraft.getInstance().particleEngine.register(CoreParticles.MAGIC_CIRCLE,
                (spriteSet) -> new MKParticle.MKParticleFactory(
                        spriteSet, -0.0001f, 0.05f,
                        0.05f, 80, true,
                        null));
        Minecraft.getInstance().particleEngine.register(CoreParticles.MAGIC_GRADIENT_SQUARE,
                (spriteSet) -> new MKParticle.MKParticleFactory(
                        spriteSet, -0.0001f, 0.05f,
                        0.05f, 80, true,
                        null));
        Minecraft.getInstance().particleEngine.register(CoreParticles.MAGIC_SIDEWAYS_LINE,
                (spriteSet) -> new MKParticle.MKParticleFactory(
                        spriteSet, -0.0001f, 0.05f,
                        0.05f, 80, true,
                        null));
        Minecraft.getInstance().particleEngine.register(CoreParticles.MAGIC_CHIP,
                (spriteSet) -> new MKParticle.MKParticleFactory(
                        spriteSet, -0.0001f, 0.05f,
                        0.05f, 80, true,
                        null));
        Minecraft.getInstance().particleEngine.register(CoreParticles.INDICATOR_PARTICLE,
                IndicatorParticle.IndicatorFactory::new);
        Minecraft.getInstance().particleEngine.register(CoreParticles.BLACK_MAGIC_CROSS,
                (spriteSet) -> new MKParticle.MKParticleFactory(
                        spriteSet, -0.0001f, 0.05f,
                        0.05f, 80, true, ParticleRenderTypes.BLACK_MAGIC_RENDERER,
                        null));
        Minecraft.getInstance().particleEngine.register(CoreParticles.BLACK_MAGIC_CLOVER,
                (spriteSet) -> new MKParticle.MKParticleFactory(
                        spriteSet, -0.0001f, 0.05f,
                        0.05f, 80, true, ParticleRenderTypes.BLACK_MAGIC_RENDERER,
                        null));
        Minecraft.getInstance().particleEngine.register(CoreParticles.BLACK_MAGIC_CIRCLE,
                (spriteSet) -> new MKParticle.MKParticleFactory(
                        spriteSet, -0.0001f, 0.05f,
                        0.05f, 80, true, ParticleRenderTypes.BLACK_MAGIC_RENDERER,
                        null));
        Minecraft.getInstance().particleEngine.register(CoreParticles.BLACK_MAGIC_LINE,
                (spriteSet) -> new MKParticle.MKParticleFactory(
                        spriteSet, -0.0001f, 0.05f,
                        0.05f, 80, true, ParticleRenderTypes.BLACK_MAGIC_RENDERER,
                        null));
        Minecraft.getInstance().particleEngine.register(CoreParticles.BLACK_MAGIC_GRADIENT_SQUARE,
                (spriteSet) -> new MKParticle.MKParticleFactory(
                        spriteSet, -0.0001f, 0.05f,
                        0.05f, 80, true, ParticleRenderTypes.BLACK_MAGIC_RENDERER,
                        null));
    }
}
