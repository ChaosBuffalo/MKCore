package com.chaosbuffalo.mkcore.test.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkcore.effects.MKSimplePassiveState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

public class PhoenixAspectEffect extends MKEffect {

    public static final UUID MODIFIER_ID = UUID.fromString("721f69b8-c361-4b80-897f-724f84e08ae7");

    public static final PhoenixAspectEffect INSTANCE = new PhoenixAspectEffect();

    private PhoenixAspectEffect() {
        super(EffectType.BENEFICIAL);
        setRegistryName("effect.test_phoenix_aspect");
        addAttribute(MKAttributes.COOLDOWN, MODIFIER_ID, 0.33, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttribute(MKAttributes.MANA_REGEN, MODIFIER_ID, 1.0f, AttributeModifier.Operation.ADDITION);
    }

    public void enableFlying(LivingEntity target) {
        if (target instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) target;
            player.abilities.allowFlying = true;
            player.sendPlayerAbilities();
        }
    }

    @Override
    public void onInstanceAdded(IMKEntityData targetData, MKActiveEffect newInstance) {
        super.onInstanceAdded(targetData, newInstance);
        enableFlying(targetData.getEntity());
    }

    @Override
    public void onInstanceReady(IMKEntityData targetData, MKActiveEffect activeInstance) {
        super.onInstanceReady(targetData, activeInstance);
        enableFlying(targetData.getEntity());
    }

    @Override
    public void onInstanceRemoved(IMKEntityData targetData, MKActiveEffect expiredEffect) {
        super.onInstanceRemoved(targetData, expiredEffect);
        if (targetData.getEntity() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) targetData.getEntity();
            player.abilities.allowFlying = false;
            player.abilities.isFlying = false;
            player.sendPlayerAbilities();
        }
    }

    @Override
    public MKEffectState makeState() {
        return MKSimplePassiveState.INSTANCE;
    }

    @SuppressWarnings("unused")
    @Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    private static class RegisterMe {
        @SubscribeEvent
        public static void register(RegistryEvent.Register<MKEffect> event) {
            event.getRegistry().register(INSTANCE);
        }
    }
}
