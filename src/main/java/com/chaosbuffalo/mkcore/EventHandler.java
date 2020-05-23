package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.core.IMKPlayerData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerDataSyncRequestPacket;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID)
public class EventHandler {

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        LivingEntity living = event.getEntityLiving();

        if (living instanceof PlayerEntity) {
            living.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(IMKPlayerData::update);
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof PlayerEntity) {
            if (event.getWorld().isRemote) {
                MKCore.LOGGER.info("client player joined world!");
                PacketHandler.sendMessageToServer(new PlayerDataSyncRequestPacket());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone evt) {
        PlayerEntity player = evt.getPlayer();
        PlayerEntity oldPlayer = evt.getOriginal();

        player.getCapability(Capabilities.PLAYER_CAPABILITY)
                .ifPresent(newCap -> oldPlayer.getCapability(Capabilities.PLAYER_CAPABILITY)
                        .ifPresent(oldCap -> newCap.clone(oldCap, evt.isWasDeath())));
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
//        MKCore.LOGGER.info("StartTracking {} {}", event.getTarget(), event.getTarget().getEntityId());
        if (event.getTarget() instanceof ServerPlayerEntity) {
            PlayerEntity player = event.getPlayer();
            ServerPlayerEntity target = (ServerPlayerEntity) event.getTarget();

            player.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(cap -> {
                ((MKPlayerData) cap).fullSyncTo(target);
            });
        }
    }
}
