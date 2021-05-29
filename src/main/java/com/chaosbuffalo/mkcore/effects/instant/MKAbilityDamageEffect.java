package com.chaosbuffalo.mkcore.effects.instant;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import com.chaosbuffalo.mkcore.effects.SpellEffectBase;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKAbilityDamageEffect extends SpellEffectBase {
    public static final String DAMAGE_TYPE = "mk_ability_damage.damage_type";
    public static final String ABILITY_ID = "mk_ability_damage.ability_id";
    public static final String MODIFIER_SCALING = "mk_ability_damage.modifier_scaling";

    public static final MKAbilityDamageEffect INSTANCE = new MKAbilityDamageEffect();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Effect> event) {
        event.getRegistry().register(INSTANCE);
    }

    public static SpellCast Create(Entity source, MKDamageType damageType, MKAbility ability,
                                   float baseDamage, float scaling, float modifierScaling) {
        return INSTANCE.newSpellCast(source).setScalingParameters(baseDamage, scaling)
                .setResourceLocation(DAMAGE_TYPE, damageType.getRegistryName())
                .setResourceLocation(ABILITY_ID, ability.getAbilityId())
                .setFloat(MODIFIER_SCALING, modifierScaling);
    }

    public static SpellCast Create(Entity source, MKDamageType damageType, MKAbility ability,
                                   float baseDamage, float scaling) {
        return Create(source, damageType, ability, baseDamage, scaling, 1.0f);
    }

    private MKAbilityDamageEffect() {
        super(EffectType.HARMFUL, 123);
        setRegistryName("effect.instant_mk_damage");
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }

    @Override
    public void doEffect(Entity applier, Entity caster, LivingEntity target, int amplifier, SpellCast cast) {
        float damage = cast.getScaledValue(amplifier);
        MKDamageType damageType = MKCoreRegistry.getDamageType(cast.getResourceLocation(DAMAGE_TYPE));
        target.attackEntityFrom(MKDamageSource.causeAbilityDamage(
                damageType, cast.getResourceLocation(ABILITY_ID), applier, caster,
                cast.getFloat(MODIFIER_SCALING)), damage);
    }
}

