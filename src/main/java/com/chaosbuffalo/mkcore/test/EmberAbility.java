package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.CastState;
import com.chaosbuffalo.mkcore.abilities.PlayerAbility;
import com.chaosbuffalo.mkcore.abilities.SingleTargetCastState;
import com.chaosbuffalo.mkcore.core.IMKPlayerData;
import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EmberAbility extends PlayerAbility {
    public static final EmberAbility INSTANCE = new EmberAbility();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<PlayerAbility> event) {
        MKCore.LOGGER.info("ember register");
        event.getRegistry().register(INSTANCE.setRegistryName(INSTANCE.getAbilityId()));
    }

    public static float BASE_DAMAGE = 6.0f;
    public static float DAMAGE_SCALE = 2.0f;
    public static int BASE_DURATION = 4;
    public static int DURATION_SCALE = 1;

    private EmberAbility() {
        super(MKCore.makeRL("ability.ember"));
    }

    @Override
    public int getCooldown(int currentRank) {
        return 6 - 2 * currentRank;
    }

    @Override
    public Targeting.TargetType getTargetType() {
        return Targeting.TargetType.ALL;
    }

    @Override
    public float getManaCost(int currentRank) {
        return 4 + currentRank * 2;
    }

    @Override
    public float getDistance(int currentRank) {
        return 25.0f;
    }

    @Override
    public int getCastTime(int currentRank) {
        return GameConstants.TICKS_PER_SECOND / 2;
    }

    @Override
    public CastState createCastState(int castTime) {
        return new SingleTargetCastState(castTime);
    }

//    @Nullable
//    @Override
//    public SoundEvent getSpellCompleteSoundEvent() {
//        return ModSounds.spell_cast_7;
//    }
//
//    @Override
//    public SoundEvent getCastingSoundEvent() {
//        return ModSounds.casting_fire;
//    }

    @Override
    public int getRequiredLevel(int currentRank) {
        return currentRank * 2;
    }

    @Override
    public void endCast(PlayerEntity entity, IMKPlayerData data, World theWorld, CastState state) {
        super.endCast(entity, data, theWorld, state);
        SingleTargetCastState singleTargetState = (SingleTargetCastState) state;
        if (singleTargetState == null) {
            return;
        }

        singleTargetState.getTarget().ifPresent(targetEntity -> {
            int level = data.getAbilityRank(getAbilityId());
            targetEntity.setFire(BASE_DURATION + level * DURATION_SCALE);
//            targetEntity.attackEntityFrom(MKDamageSource.causeIndirectMagicDamage(getAbilityId(), entity, entity), BASE_DAMAGE + level * DAMAGE_SCALE);
            targetEntity.attackEntityFrom(DamageSource.causeIndirectMagicDamage(entity, entity), BASE_DAMAGE + level * DAMAGE_SCALE);
//            AbilityUtils.playSoundAtServerEntity(targetEntity, ModSounds.spell_fire_6, SoundCategory.PLAYERS);
//            Vec3d lookVec = entity.getLookVec();
//            MKUltra.packetHandler.sendToAllAround(
//                    new ParticleEffectSpawnPacket(
//                            EnumParticleTypes.FLAME.getParticleID(),
//                            ParticleEffects.CIRCLE_PILLAR_MOTION, 60, 10,
//                            targetEntity.posX, targetEntity.posY + 1.0,
//                            targetEntity.posZ, 1.0, 1.0, 1.0, 1.0,
//                            lookVec),
//                    entity.dimension, targetEntity.posX,
//                    targetEntity.posY, targetEntity.posZ, 50.0f);
        });
    }

    @Override
    public void execute(PlayerEntity entity, IMKPlayerData pData, World theWorld) {
        MKCore.LOGGER.info("ember execute");
        int level = pData.getAbilityRank(getAbilityId());
        LivingEntity targetEntity = getSingleLivingTarget(entity, getDistance(level));
        MKCore.LOGGER.info("ember target {}", targetEntity);
        if (targetEntity != null) {
            CastState state = pData.startAbility(this);
            SingleTargetCastState singleTargetState = (SingleTargetCastState) state;
            if (singleTargetState != null) {
                singleTargetState.setTarget(targetEntity);
            }
        }
    }
}
