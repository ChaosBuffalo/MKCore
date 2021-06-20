package com.chaosbuffalo.mkcore.fx.particles;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.ParticleAnimationTrack;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.ParticleColorAnimationTrack;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.ParticleRenderScaleAnimationTrack;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions.BrownianMotionTrack;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions.InheritMotionTrack;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions.LinearMotionTrack;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions.OrbitingInPlaneMotionTrack;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleAnimationsSyncPacket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.network.NetworkDirection;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ParticleAnimationManager extends JsonReloadListener {
    private MinecraftServer server;
    private boolean serverStarted = false;

    public static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    public static final Map<ResourceLocation, Supplier<ParticleAnimationTrack>> TRACK_DESERIALIZERS = new HashMap<>();
    public static final Map<ResourceLocation, ParticleAnimation> ANIMATIONS = new HashMap<>();

    public ParticleAnimationManager() {
        super(GSON, "particle_animations");
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
        ANIMATIONS.clear();
        for(Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            MKCore.LOGGER.info("Particle Animation Definition file: {}", resourcelocation);
            if (resourcelocation.getPath().startsWith("_")) continue; //Forge: filter anything beginning with "_" as it's used for metadata.
            ParticleAnimation anim = ParticleAnimation.deserializeFromDynamic(entry.getKey(),
                    new Dynamic<>(JsonOps.INSTANCE, entry.getValue()));
            ANIMATIONS.put(entry.getKey(), anim);
        }
        if (serverStarted){
            syncToPlayers();
        }
    }

    @SubscribeEvent
    public void serverStop(FMLServerStoppingEvent event) {
        serverStarted = false;
        server = null;
    }

    @SubscribeEvent
    public void serverStart(FMLServerAboutToStartEvent event) {
        server = event.getServer();
        serverStarted = true;
    }

    @SubscribeEvent
    public void playerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event){
        if (event.getPlayer() instanceof ServerPlayerEntity){
            ParticleAnimationsSyncPacket updatePacket = new ParticleAnimationsSyncPacket(ANIMATIONS);
            MKCore.LOGGER.info("Sending {} particle animation sync packet", event.getPlayer());
            ((ServerPlayerEntity) event.getPlayer()).connection.sendPacket(
                    PacketHandler.getNetworkChannel().toVanillaPacket(
                            updatePacket, NetworkDirection.PLAY_TO_CLIENT));
        }
    }

    public void syncToPlayers(){
        if (server != null){
            ParticleAnimationsSyncPacket updatePacket = new ParticleAnimationsSyncPacket(ANIMATIONS);
            server.getPlayerList().sendPacketToAllPlayers(PacketHandler.getNetworkChannel().toVanillaPacket(
                    updatePacket, NetworkDirection.PLAY_TO_CLIENT));
        }
    }
}
