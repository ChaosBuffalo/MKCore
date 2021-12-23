package com.chaosbuffalo.mkcore.mixins;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixins {

    @Shadow protected abstract boolean canBlockDamageSource(DamageSource damageSourceIn);

    @Shadow public abstract boolean isHandActive();

    @Shadow protected ItemStack activeItemStack;

    @Shadow protected int activeItemStackUseCount;

    // disable player blocking as we handle it ourselves
    @Redirect(at = @At(value = "INVOKE", target="Lnet/minecraft/entity/LivingEntity;canBlockDamageSource(Lnet/minecraft/util/DamageSource;)Z"),
            method = "Lnet/minecraft/entity/LivingEntity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z")
    private boolean proxyCanBlockDamageSource(LivingEntity entity, DamageSource damageSourceIn){
        if (entity instanceof PlayerEntity){
            return false;
        } else {
            return canBlockDamageSource(damageSourceIn);
        }
    }

    public boolean isActiveItemStackBlocking() {
        if (isHandActive() && !activeItemStack.isEmpty()) {
            Item item = this.activeItemStack.getItem();
            if (item.getUseAction(this.activeItemStack) != UseAction.BLOCK) {
                return false;
            } else {
                return item.getUseDuration(this.activeItemStack) - activeItemStackUseCount >= 1;
            }
        } else {
            return false;
        }
    }
}
