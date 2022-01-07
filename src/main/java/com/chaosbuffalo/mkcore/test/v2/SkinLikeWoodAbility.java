package com.chaosbuffalo.mkcore.test.v2;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKToggleAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SkinLikeWoodAbility extends MKToggleAbility {
    public static SkinLikeWoodAbility INSTANCE = new SkinLikeWoodAbility();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKAbility> event) {
        event.getRegistry().register(INSTANCE);
    }

    public SkinLikeWoodAbility() {
        super(MKCore.MOD_ID, "ability.v2.skin_like_wood");
        setCooldownSeconds(6);
        setManaCost(4);
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.SELF;
    }

    @Override
    public MKEffect getToggleEffect() {
        return SkinLikeWoodEffect.INSTANCE;
    }

    @Override
    public void applyEffect(LivingEntity castingEntity, IMKEntityData casterData) {
        super.applyEffect(castingEntity, casterData);

        MKEffectBuilder<?> instance = getToggleEffect().builder(castingEntity.getUniqueID())
                .amplify(2)
                .infinite();
        casterData.getEffects().addEffect(instance);
    }
}
