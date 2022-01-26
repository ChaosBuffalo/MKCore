package com.chaosbuffalo.mkcore.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface IReceivesSkillChange {

    void onSkillChange(ItemStack itemStack, PlayerEntity player);
}
