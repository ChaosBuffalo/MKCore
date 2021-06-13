package com.chaosbuffalo.mkcore.client.rendering;

import com.chaosbuffalo.mkcore.MKConfig;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.entities.MKAreaEffectEntity;
import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleKeyFrame;
import com.chaosbuffalo.mkcore.init.CoreParticles;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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

    private static final ParticleAnimation BlueMagicAnimation = new ParticleAnimation()
            .withKeyFrame(new ParticleKeyFrame()
                    .withColor(0.0f, 1.0f, 242.0f / 255.0f)
                    .withScale(0.75f, 0.25f)
            )
            .withKeyFrame(new ParticleKeyFrame(0, 100)
                    .withColor(0.0f, 0.5f, 0.5f)
                    .withScale(0.4f, .2f)
            )
            .withKeyFrame(new ParticleKeyFrame(100, 100)
                    .withColor(1.0f, 0.0f, 0.75f)
                    .withScale(.01f, 0.0f)
            );


    @SubscribeEvent
    public static void registerParticleFactory(ParticleFactoryRegisterEvent evt){
        Minecraft.getInstance().particles.registerFactory(CoreParticles.BLUE_MAGIC_CROSS,
                (spriteSet) -> new MKParticle.MKParticleFactory(
                        spriteSet, false, -0.0001f, 0.05f,
                        0.05f, 200, true,
                        null, BlueMagicAnimation));
    }
}
