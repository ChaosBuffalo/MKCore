package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.item.ILimitItemTooltip;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemStack.class)
public abstract class ItemStackMixins {

    @Shadow public abstract Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot);

    @Redirect(method= "Lnet/minecraft/item/ItemStack;getTooltip(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/client/util/ITooltipFlag;)Ljava/util/List;",
            at=@At(target="Lnet/minecraft/item/ItemStack;getAttributeModifiers(Lnet/minecraft/inventory/EquipmentSlotType;)Lcom/google/common/collect/Multimap;", value="INVOKE"))
    private Multimap<Attribute, AttributeModifier> proxyGetAttributeModifiers(ItemStack itemStack, EquipmentSlotType equipmentSlot){
        Multimap<Attribute, AttributeModifier> attrs = getAttributeModifiers(equipmentSlot);
        if (itemStack.getItem() instanceof ILimitItemTooltip){
            Multimap<Attribute, AttributeModifier> newMap = HashMultimap.create();
            newMap.putAll(attrs);
            return ((ILimitItemTooltip) itemStack.getItem()).limitTooltip(itemStack, equipmentSlot, newMap);
        } else {
            return attrs;
        }
    }
}
