package com.chaosbuffalo.mkcore.fx.particles;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.ParticleAnimationTrack;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.ParticleColorAnimationTrack;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.ParticleRenderScaleAnimationTrack;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions.BrownianMotionTrack;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions.InheritMotionTrack;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions.LinearMotionTrack;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions.OrbitingInPlaneMotionTrack;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ParticleAnimationManager extends JsonReloadListener {

    public static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    public static final Map<ResourceLocation, Supplier<ParticleAnimationTrack>> TRACK_DESERIALIZERS = new HashMap<>();
    public static final Map<ResourceLocation, ParticleAnimation> ANIMATIONS = new HashMap<>();

    public ParticleAnimationManager() {
        super(GSON, "mk_particle_animations");
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static void setupDeserializers(){
        putTrackDeserializer(ParticleRenderScaleAnimationTrack.TYPE_NAME, ParticleRenderScaleAnimationTrack::new);
        putTrackDeserializer(ParticleColorAnimationTrack.TYPE_NAME, ParticleColorAnimationTrack::new);
        putTrackDeserializer(BrownianMotionTrack.TYPE_NAME, BrownianMotionTrack::new);
        putTrackDeserializer(InheritMotionTrack.TYPE_NAME, InheritMotionTrack::new);
        putTrackDeserializer(LinearMotionTrack.TYPE_NAME, LinearMotionTrack::new);
        putTrackDeserializer(OrbitingInPlaneMotionTrack.TYPE_NAME, OrbitingInPlaneMotionTrack::new);
    }

    public static void putTrackDeserializer(ResourceLocation name, Supplier<ParticleAnimationTrack> supplier){
        TRACK_DESERIALIZERS.put(name, supplier);
    }

    @Nullable
    public static ParticleAnimationTrack getAnimationTrack(ResourceLocation trackName){

        if (!TRACK_DESERIALIZERS.containsKey(trackName)){
            MKCore.LOGGER.error("Failed to deserialize animation track {}", trackName);
            return null;
        }
        return TRACK_DESERIALIZERS.get(trackName).get();
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn) {

    }
}
