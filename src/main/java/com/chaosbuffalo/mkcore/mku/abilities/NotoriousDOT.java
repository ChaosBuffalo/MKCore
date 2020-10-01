package com.chaosbuffalo.mkcore.mku.abilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilityTargetSelector;
import com.chaosbuffalo.mkcore.abilities.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKSongAbility;
import com.chaosbuffalo.mkcore.abilities.ai.conditions.MeleeUseCondition;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.init.ModSounds;
import com.chaosbuffalo.mkcore.mku.effects.NotoriousDOTCasterEffect;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NotoriousDOT extends MKSongAbility {
    public static final NotoriousDOT INSTANCE = new NotoriousDOT();
    public static ResourceLocation TOGGLE_GROUP = MKCore.makeRL("toggle_group.skald");

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKAbility> event) {
        event.getRegistry().register(INSTANCE);
    }

    public static float BASE_DAMAGE = 1.0f;
    public static float DAMAGE_SCALE = 2.0f;
    public static int BASE_DURATION = 32767;

    private NotoriousDOT() {
        super(MKCore.makeRL("ability.notorious_dot"));
        setUseCondition(new MeleeUseCondition(this));
    }

    @Override
    public Effect getToggleEffect() {
        return NotoriousDOTCasterEffect.INSTANCE;
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.SELF;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.PBAOE;
    }

    @Override
    public float getDistance() {
        return 6f;
    }

    @Override
    public ResourceLocation getToggleGroupId() {
        return TOGGLE_GROUP;
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return null;
    }

    @Override
    public void applyEffect(LivingEntity entity, IMKEntityData entityData) {
        super.applyEffect(entity, entityData);
        int level = 1;
        entity.addPotionEffect(NotoriousDOTCasterEffect.INSTANCE.createSelfCastEffectInstance(entity, level));
        SoundUtils.playSoundAtEntity(entity, ModSounds.spell_shadow_9);
        PacketHandler.sendToTrackingMaybeSelf(
                new ParticleEffectSpawnPacket(
                        ParticleTypes.NOTE,
                        ParticleEffects.SPHERE_MOTION, 50, 5,
                        entity.getPosX(), entity.getPosY() + 1.0,
                        entity.getPosZ(), 1.0, 1.0, 1.0, 1.0f,
                        entity.getLookVec()), entity);
    }
}
