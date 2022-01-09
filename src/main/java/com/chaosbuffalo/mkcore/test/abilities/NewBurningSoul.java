package com.chaosbuffalo.mkcore.test.abilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKPassiveAbility;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.test.effects.NewBurningSoulEffect;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class NewBurningSoul extends MKPassiveAbility {
    public static final NewBurningSoul INSTANCE = new NewBurningSoul();

    public NewBurningSoul() {
        super(MKCore.makeRL("ability.v2.burning_soul"));
    }

    @Override
    public MKEffect getPassiveEffect() {
        return NewBurningSoulEffect.INSTANCE;
    }

    @SuppressWarnings("unused")
    @Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    private static class RegisterMe {
        @SubscribeEvent
        public static void register(RegistryEvent.Register<MKAbility> event) {
            event.getRegistry().register(INSTANCE);
        }
    }
}
