package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.CastInterruptReason;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.init.CoreSounds;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerLeftClickEmptyPacket;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CombatEventHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity livingTarget = event.getEntityLiving();
        if (livingTarget.world.isRemote)
            return;

        DamageSource source = event.getSource();
        Entity trueSource = source.getTrueSource();
        if (source == DamageSource.FALL) { // TODO: maybe just use LivingFallEvent?
            SpellTriggers.FALL.onLivingFall(event, source, livingTarget);
        }

        // Living is source
        if (trueSource instanceof LivingEntity) {
            MKCore.getEntityData(trueSource).ifPresent(
                    (sourceData) -> SpellTriggers.LIVING_HURT_ENTITY.onLivingHurtEntity(event, source, livingTarget,
                            (LivingEntity) trueSource, sourceData)
            );
        }

        // Living is victim
        MKCore.getEntityData(livingTarget).ifPresent(targetData ->
                SpellTriggers.ENTITY_HURT_LIVING.onEntityHurtLiving(event, source, livingTarget, targetData));
    }

    @SubscribeEvent
    public static void onAttackEntityEvent(AttackEntityEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player.world.isRemote)
            return;
        Entity target = event.getTarget();

        SpellTriggers.PLAYER_ATTACK_ENTITY.onAttackEntity(player, target);
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        if (event.getPlayer().world.isRemote) {
            // Only send this spammy packet if someone will listen to it
            if (SpellTriggers.EMPTY_LEFT_CLICK.hasTriggers()) {
                PacketHandler.sendMessageToServer(new PlayerLeftClickEmptyPacket());
            }
        }
    }

    @SubscribeEvent
    public static void onLeftClickEmptyServer(ServerSideLeftClickEmpty event) {
        if (!event.getPlayer().world.isRemote) {
            SpellTriggers.EMPTY_LEFT_CLICK.onEmptyLeftClick(event.getPlayer(), event);
        }
    }

    private static boolean canBlock(DamageSource source, LivingEntity entity){

        Entity sourceEntity = source.getImmediateSource();
        boolean hasPiercing = false;
        if (sourceEntity instanceof AbstractArrowEntity) {
            AbstractArrowEntity abstractarrowentity = (AbstractArrowEntity)sourceEntity;
            if (abstractarrowentity.getPierceLevel() > 0) {
                hasPiercing = true;
            }
        }

        if (!source.isUnblockable() && entity.isActiveItemStackBlocking() && !hasPiercing) {
            Vector3d damageLoc = source.getDamageLocation();
            if (damageLoc != null) {
                Vector3d lookVec = entity.getLook(1.0F);
                Vector3d damageDir = damageLoc.subtractReverse(entity.getPositionVec()).normalize();
                damageDir = new Vector3d(damageDir.x, 0.0D, damageDir.z);
                if (damageDir.dotProduct(lookVec) < 0.0D) {
                    return true;
                }
            }
        }
        return false;

    }

    @SubscribeEvent
    public static void onLivingAttackEvent(LivingAttackEvent event) {
        Entity target = event.getEntity();
        if (target.world.isRemote)
            return;

        DamageSource dmgSource = event.getSource();
        Entity source = dmgSource.getTrueSource();

        if (canBlock(dmgSource, event.getEntityLiving())){
            MKCore.getPlayer(target).ifPresent(playerData -> {
                Tuple<Float, Boolean> breakResult = playerData.getStats().handlePoiseDamage(event.getAmount());
                float left = breakResult.getA();
                if (!(dmgSource instanceof MKDamageSource)){
                    // correct for if we're a vanilla damage source and we're going to bypass armor so pre-apply armor
                    if (SpellTriggers.isProjectileDamage(dmgSource)){
                        left = CoreDamageTypes.RangedDamage.applyResistance(event.getEntityLiving(), left);
                    } else {
                        left = CoreDamageTypes.MeleeDamage.applyResistance(event.getEntityLiving(), left);
                    }

                }
                event.setCanceled(true);
                if (left > 0){
                    target.attackEntityFrom(dmgSource instanceof MKDamageSource ? ((MKDamageSource) dmgSource)
                            .setSuppressTriggers(true).setDamageBypassesArmor() : dmgSource.setDamageBypassesArmor(),
                            left);
                }
                if (breakResult.getB()){
                    SoundUtils.serverPlaySoundAtEntity(event.getEntityLiving(),
                            CoreSounds.block_break, event.getEntityLiving().getSoundCategory());
                } else {
                    if (event.getEntityLiving().getItemInUseMaxCount() <= 6){
                        SoundUtils.serverPlaySoundAtEntity(event.getEntityLiving(),
                                CoreSounds.parry, event.getEntityLiving().getSoundCategory());
                    }
                    else if (dmgSource.getImmediateSource() instanceof AbstractArrowEntity){
                        SoundUtils.serverPlaySoundAtEntity(event.getEntityLiving(),
                                CoreSounds.arrow_block, event.getEntityLiving().getSoundCategory());
                    } else if (source instanceof LivingEntity){
                        if (((LivingEntity) source).getHeldItem(Hand.MAIN_HAND).getItem() instanceof SwordItem){
                            SoundUtils.serverPlaySoundAtEntity(event.getEntityLiving(),
                                    CoreSounds.weapon_block, event.getEntityLiving().getSoundCategory());
                        } else {
                            SoundUtils.serverPlaySoundAtEntity(event.getEntityLiving(),
                                    CoreSounds.fist_block, event.getEntityLiving().getSoundCategory());
                        }
                    }
                }

            });
        }
        if (dmgSource instanceof MKDamageSource) {
            if (((MKDamageSource) dmgSource).shouldSuppressTriggers())
                return;
        }
        if (source instanceof LivingEntity) {
            SpellTriggers.ATTACK_ENTITY.onAttackEntity((LivingEntity) source, target);
        }
    }

    @SubscribeEvent
    public static void onArrowImpact(ProjectileImpactEvent.Arrow arrowEvent) {
        Entity shooter = arrowEvent.getArrow().getShooter(); // getShooter
        if (shooter != null) {
            MKCore.getEntityData(shooter).ifPresent(cap -> {
                if (arrowEvent.getRayTraceResult().getType() == RayTraceResult.Type.BLOCK) {
                    cap.getCombatExtension().projectileMiss();
                } else {
                    cap.getCombatExtension().recordProjectileHit();
                }
            });
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        MKCore.getEntityData(event.getEntityLiving()).ifPresent(entityData ->
                entityData.getAbilityExecutor().interruptCast(CastInterruptReason.Death));

        DamageSource source = event.getSource();
        if (source.getTrueSource() instanceof LivingEntity) {
            LivingEntity killer = (LivingEntity) source.getTrueSource();
            if (killer.world.isRemote) {
                return;
            }
            SpellTriggers.LIVING_KILL_ENTITY.onEntityDeath(event, source, killer);
        }

        SpellTriggers.LIVING_DEATH.onEntityDeath(event, source, event.getEntityLiving());
    }


}
