package com.chaosbuffalo.mkcore.mku.abilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKToggleAbility;
import com.chaosbuffalo.mkcore.abilities.ai.conditions.NeedsBuffCondition;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.PassiveEffect;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.init.ModSounds;
import com.chaosbuffalo.mkcore.mku.effects.SkinLikeWoodEffect;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SkinLikeWoodAbility extends MKToggleAbility {
    public static final SkinLikeWoodAbility INSTANCE = new SkinLikeWoodAbility();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKAbility> event) {
        event.getRegistry().register(INSTANCE);
    }

    private SkinLikeWoodAbility() {
        super(MKCore.makeRL("ability.skin_like_wood"));
        setCooldownSeconds(3);
        setManaCost(2);
        setUseCondition(new NeedsBuffCondition(this, SkinLikeWoodEffect.INSTANCE).setSelfOnly(true));
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.SELF;
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 1.0f;
    }

    @Override
    public PassiveEffect getToggleEffect() {
        return SkinLikeWoodEffect.INSTANCE;
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return null;
    }

    @Override
    public void applyEffect(LivingEntity entity, IMKEntityData entityData) {
        super.applyEffect(entity, entityData);
        int amplifier = 0;
        SoundUtils.playSoundAtEntity(entity, ModSounds.spell_earth_7);
        // What to do for each target hit
        entity.addPotionEffect(getToggleEffect().createSelfCastEffectInstance(entity, amplifier));

        PacketHandler.sendToTrackingAndSelf(new ParticleEffectSpawnPacket(
                        ParticleTypes.ITEM_SLIME,
                        ParticleEffects.CIRCLE_MOTION, 30, 0,
                        entity.getPosX(), entity.getPosY() + .5,
                        entity.getPosZ(), 1.0, 1.0, 1.0, 1.0f,
                        entity.getLookVec()), entity);
    }
}
