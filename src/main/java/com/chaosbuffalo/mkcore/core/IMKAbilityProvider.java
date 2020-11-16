package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import net.minecraft.item.ItemStack;

public interface IMKAbilityProvider {
    MKAbility getAbility(ItemStack item);
}
