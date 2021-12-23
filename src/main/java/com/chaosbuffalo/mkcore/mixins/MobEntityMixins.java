package com.chaosbuffalo.mkcore.mixins;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MobEntity.class)
public class MobEntityMixins {

    //disables vanilla entity block breaking since it makes less sense with the poise system
    private void func_233655_a_(PlayerEntity p_233655_1_, ItemStack p_233655_2_, ItemStack p_233655_3_){

    }
}
