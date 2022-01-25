package com.chaosbuffalo.mkcore.mixins;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MobEntity.class)
public class MobEntityMixins {

    /**
     * @author kovak
     * @reason disables vanilla entity block breaking since it makes less sense with the poise system
     * <p>
     * Real name maybeDisableShield
     */
    @Overwrite
    private void func_233655_a_(PlayerEntity p_233655_1_, ItemStack p_233655_2_, ItemStack p_233655_3_) {

    }
}
