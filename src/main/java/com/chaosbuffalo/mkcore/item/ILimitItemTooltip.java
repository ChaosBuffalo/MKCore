package com.chaosbuffalo.mkcore.item;

import com.google.common.collect.Multimap;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

public interface ILimitItemTooltip {

    Multimap<Attribute, AttributeModifier> limitTooltip(ItemStack itemStack, EquipmentSlotType equipmentSlot, Multimap<Attribute, AttributeModifier> modifiers);
}
