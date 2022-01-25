package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.item.AttributeTooltipManager;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackClientMixins {

    @Unique
    private List<ITextComponent> tooltipList;

    @Unique
    private PlayerEntity player;

    @Shadow
    public abstract Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot);

    // lets us remove attributes only from the automatic tooltip generation in item stack
    @Redirect(
            method = "Lnet/minecraft/item/ItemStack;getTooltip(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/client/util/ITooltipFlag;)Ljava/util/List;",
            at = @At(
                    target = "Lnet/minecraft/item/ItemStack;getAttributeModifiers(Lnet/minecraft/inventory/EquipmentSlotType;)Lcom/google/common/collect/Multimap;",
                    value = "INVOKE"
            )
    )
    private Multimap<Attribute, AttributeModifier> proxyGetAttributeModifiers(ItemStack itemStack, EquipmentSlotType equipmentSlot) {
        // Don't follow our path if it's building the search tree during startup
        if (player == null) {
            return getAttributeModifiers(equipmentSlot);
        }

        if (tooltipList != null && player != null) {
            AttributeTooltipManager.renderTooltip(tooltipList, player, itemStack, equipmentSlot);
        }
        return ImmutableMultimap.of();
    }

    @ModifyVariable(
            method = "Lnet/minecraft/item/ItemStack;getTooltip(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/client/util/ITooltipFlag;)Ljava/util/List;",
            at = @At("HEAD"),
            argsOnly = true
    )
    private PlayerEntity capturePlayer(PlayerEntity player) {
        this.player = player;
        return player;
    }

    @ModifyVariable(
            method = "Lnet/minecraft/item/ItemStack;getTooltip(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/client/util/ITooltipFlag;)Ljava/util/List;",
            at = @At("STORE"),
            index = 3,
            ordinal = 0
    )
    private List<ITextComponent> captureList(List<ITextComponent> list) {
        this.tooltipList = list;
        return list;
    }
}
