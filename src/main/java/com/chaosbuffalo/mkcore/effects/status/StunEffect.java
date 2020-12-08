package com.chaosbuffalo.mkcore.effects.status;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.effects.PassiveEffect;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class StunEffect extends PassiveEffect {
    public static final UUID MODIFIER_ID = UUID.fromString("2d012acc-43ac-40e6-a37c-e6ac5dfd47f2");

    public static final StunEffect INSTANCE = (StunEffect) new StunEffect(0)
            .addAttributesModifier(Attributes.MOVEMENT_SPEED, MODIFIER_ID.toString(), -1,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);

    protected StunEffect(int liquidColorIn) {
        super(EffectType.HARMFUL, liquidColorIn);
        setRegistryName(MKCore.MOD_ID, "effect.stun");
    }

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Effect> event) {
        event.getRegistry().register(INSTANCE);
    }

    public static SpellCast Create(Entity source) {
        return INSTANCE.newSpellCast(source);
    }

    @Override
    public void onPotionAdd(SpellCast cast, LivingEntity target, AttributeModifierManager attributes, int amplifier) {
        super.onPotionAdd(cast, target, attributes, amplifier);
        if (target instanceof MobEntity) {
            MobEntity mob = (MobEntity) target;
            mob.setNoAI(true);
        }
        MKCore.getEntityData(target).ifPresent(entityData -> entityData.getAbilityExecutor().interruptCast());
    }

    @Override
    public void onPotionRemove(SpellCast cast, LivingEntity target, AttributeModifierManager attributes, int amplifier) {
        super.onPotionRemove(cast, target, attributes, amplifier);
        if (target instanceof MobEntity) {
            MobEntity mob = (MobEntity) target;
            mob.setNoAI(false);
        }
    }
}

