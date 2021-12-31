package com.chaosbuffalo.mkcore.test.v2;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKPassiveAbility;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NewBurningSoul extends MKPassiveAbility {
    public static final NewBurningSoul INSTANCE = new NewBurningSoul();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKAbility> event) {
        event.getRegistry().register(INSTANCE);
    }

    public NewBurningSoul() {
        super(MKCore.makeRL("ability.v2.burning_soul"));
    }

    @Override
    public MKEffect getPassiveEffect() {
        return NewBurningSoulEffect.INSTANCE;
    }
}
