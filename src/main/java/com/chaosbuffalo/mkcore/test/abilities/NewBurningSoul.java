package com.chaosbuffalo.mkcore.test.abilities;

import com.chaosbuffalo.mkcore.abilities.MKPassiveAbility;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.test.effects.NewBurningSoulEffect;

public class NewBurningSoul extends MKPassiveAbility {
    public static final NewBurningSoul INSTANCE = new NewBurningSoul();

    public NewBurningSoul() {
        super();
    }

    @Override
    public MKEffect getPassiveEffect() {
        return NewBurningSoulEffect.INSTANCE;
    }
}
