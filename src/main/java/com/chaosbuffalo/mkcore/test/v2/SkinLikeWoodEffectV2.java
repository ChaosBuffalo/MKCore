package com.chaosbuffalo.mkcore.test.v2;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.effects.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.EffectType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SkinLikeWoodEffectV2 extends MKEffect {
    public static final SkinLikeWoodEffectV2 INSTANCE = new SkinLikeWoodEffectV2();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKEffect> event) {
        event.getRegistry().register(INSTANCE);
    }

    private SkinLikeWoodEffectV2() {
        super(EffectType.BENEFICIAL);
        setRegistryName("effect.v2.skin_like_wood");
        addAttribute(Attributes.ARMOR, UUID.fromString("4b488b68-1151-4bae-b99e-b381707a6964"), 2, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public MKEffectState makeState() {
        return MKSimplePassiveState.INSTANCE;
    }
}
