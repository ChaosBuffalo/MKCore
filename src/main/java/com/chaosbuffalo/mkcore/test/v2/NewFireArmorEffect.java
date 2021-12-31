package com.chaosbuffalo.mkcore.test.v2;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectInstance;
import com.chaosbuffalo.mkcore.effects.MKSimplePassiveEffect;
import net.minecraft.potion.EffectType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NewFireArmorEffect extends MKEffect {

    public static final NewFireArmorEffect INSTANCE = new NewFireArmorEffect();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKEffect> event) {
        event.getRegistry().register(INSTANCE);
    }

    private NewFireArmorEffect() {
        super(EffectType.BENEFICIAL);
        setRegistryName("effect.v2.fire_armor_effect");
    }

    @Override
    public MKEffectInstance createInstance(UUID sourceId) {
        return new MKSimplePassiveEffect(this, sourceId);
    }

}
