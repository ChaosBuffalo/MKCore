package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.utils.DamageUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;

import javax.annotation.Nullable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixins {

    @Unique
    private DamageSource damageSource;

    @Shadow
    protected abstract boolean canBlockDamageSource(DamageSource damageSourceIn);

    @Shadow
    public abstract boolean isHandActive();

    @Shadow
    protected ItemStack activeItemStack;

    @Shadow
    protected int activeItemStackUseCount;

    @Shadow
    @Nullable
    public abstract DamageSource getLastDamageSource();

    @Shadow
    protected abstract void damageEntity(DamageSource damageSrc, float damageAmount);

    // disable player blocking as we handle it ourselves
    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;canBlockDamageSource(Lnet/minecraft/util/DamageSource;)Z"),
            method = "Lnet/minecraft/entity/LivingEntity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"
    )
    private boolean proxyCanBlockDamageSource(LivingEntity entity, DamageSource damageSourceIn) {
        if (entity instanceof PlayerEntity) {
            return false;
        } else {
            return canBlockDamageSource(damageSourceIn);
        }
    }

    @ModifyVariable(
            method = "Lnet/minecraft/entity/LivingEntity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z",
            at = @At("HEAD"),
            index = 1,
            ordinal = 0
    )
    private DamageSource captureSource(DamageSource source) {
        this.damageSource = source;
        return source;
    }

    @ModifyConstant(
            method = "Lnet/minecraft/entity/LivingEntity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z",
            constant = @Constant(floatValue = 10.0f)
    )
    private float calculateInvulnerability(float value) {
        if (DamageUtils.isMKDamage(damageSource) ||
                DamageUtils.isMinecraftPhysicalDamage(damageSource) ||
                DamageUtils.isProjectileDamage(damageSource)) {
            return 100.0f;
        }
        return value;
    }

    @ModifyConstant(
            method = "Lnet/minecraft/entity/LivingEntity;isActiveItemStackBlocking()Z",
            constant = @Constant(intValue = 5)
    )
    private int calculateBlockDelay(int value) {
        return 1;
    }
}
