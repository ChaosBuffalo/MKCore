package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.CoreCapabilities;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.CastInterruptReason;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.effects.status.StunEffect;
import com.chaosbuffalo.mkcore.entities.IUpdateEngineProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID)
public class EntityEventHandler {

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        LivingEntity living = event.getEntityLiving();

        if (living instanceof PlayerEntity) {
            living.getCapability(CoreCapabilities.PLAYER_CAPABILITY).ifPresent(MKPlayerData::update);
        } else {
            living.getCapability(CoreCapabilities.ENTITY_CAPABILITY).ifPresent(MKEntityData::update);
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity().getEntityWorld().isRemote())
            return;

        if (event.getEntity() instanceof LivingEntity) {
            MKCore.getEntityData(event.getEntity()).ifPresent(IMKEntityData::onJoinWorld);
        }
    }

    @SubscribeEvent
    public static void onPlayerGainXP(PlayerXpEvent.XpChange event) {
        int amount = event.getAmount();
        if (event.getPlayer() instanceof ServerPlayerEntity){
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) event.getPlayer();
            Team team = event.getPlayer().getTeam();
            MinecraftServer server = serverPlayer.getServer();
            if (team != null && server != null){
                List<PlayerEntity> playersInRange = team.getMembershipCollection().stream().map(
                        x -> server.getPlayerList().getPlayerByUsername(x)).filter(
                                other -> other != null && serverPlayer.getDistanceSq(other) <= 100.0 * 100.0)
                        .collect(Collectors.toList());
                if (!playersInRange.isEmpty()){
                    amount = amount / playersInRange.size();
                }
                if (amount < 1){
                    amount = 1;
                }
                for (PlayerEntity player : playersInRange){
                    if (!player.isEntityEqual(serverPlayer)){
                        player.giveExperiencePoints(amount);
                    }
                }

            }
        }
        event.setAmount(amount);
        int finalAmount = amount;
        MKCore.getPlayer(event.getPlayer()).ifPresent(data -> {
            data.getTalents().addTalentXp(finalAmount);
        });
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone evt) {
        PlayerEntity player = evt.getPlayer();
        PlayerEntity oldPlayer = evt.getOriginal();

        MKCore.getPlayer(player)
                .ifPresent(newCap -> MKCore.getPlayer(oldPlayer)
                        .ifPresent(oldCap -> newCap.clone(oldCap, evt.isWasDeath())));
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
//        MKCore.LOGGER.info("StartTracking {} {}", event.getTarget(), event.getTarget().getEntityId());
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            ServerPlayerEntity playerEntity = (ServerPlayerEntity) event.getPlayer();

            MKCore.getEntityData(event.getTarget()).ifPresent(targetData -> targetData.onPlayerStartTracking(playerEntity));
            if (event.getTarget() instanceof IUpdateEngineProvider) {
                ((IUpdateEngineProvider) event.getTarget()).getUpdateEngine().sendAll(playerEntity);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJump(LivingEvent.LivingJumpEvent event) {
        MKCore.getEntityData(event.getEntity()).ifPresent(entityData -> {
            entityData.getAbilityExecutor().interruptCast(CastInterruptReason.Jump);
            if (entityData.getEffects().isEffectActive(StunEffect.INSTANCE)) {
                event.getEntity().setMotion(0, 0, 0);
            }
        });
    }
}
