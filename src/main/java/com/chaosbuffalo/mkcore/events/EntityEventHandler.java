package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.CoreCapabilities;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.effects.PassiveTalentEffect;
import com.chaosbuffalo.mkcore.effects.status.StunEffect;
import com.chaosbuffalo.mkcore.effects.status.StunEffectV2;
import com.chaosbuffalo.mkcore.entities.IUpdateEngineProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
        MKCore.getPlayer(event.getPlayer()).ifPresent(data -> {
            data.getTalents().addTalentXp(event.getAmount());
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
    public static void onPotionRemove(PotionEvent.PotionRemoveEvent event) {
//        MKCore.LOGGER.info("PotionRemoveEvent - {} - {}", event.getEntityLiving(), event.getPotion());

        if (event.getEntityLiving() instanceof ServerPlayerEntity && event.getPotion() instanceof PassiveTalentEffect) {
            MKCore.getPlayer(event.getEntityLiving()).ifPresent(playerData -> {
                if (!playerData.getLoadout().getPassiveGroup().canRemovePassiveEffects()) {
                    MKCore.LOGGER.info("Effect {} is a passive and passives are not unlocked", event.getPotion());
                    event.setCanceled(true);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onEntityJump(LivingEvent.LivingJumpEvent event) {
        MKCore.getEntityData(event.getEntity()).ifPresent(entityData -> {
            entityData.getAbilityExecutor().interruptCast();
            if (entityData.getEntity().isPotionActive(StunEffect.INSTANCE) ||
                    entityData.getEffects().isEffectActive(StunEffectV2.INSTANCE)) {
                event.getEntity().setMotion(0, 0, 0);
            }
        });
    }
}