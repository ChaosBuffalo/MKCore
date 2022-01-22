package com.chaosbuffalo.mkcore.item;

import com.google.common.collect.Multimap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.text.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.function.DoubleFunction;

public class AttributeTooltipManager {

    // Why are these protected in Item? Not sure it's worth an AT
    protected static final UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    protected static final UUID ATTACK_SPEED_MODIFIER = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");

    public interface ItemAttributeRenderer {
        List<ITextComponent> render(ItemStack stack, EquipmentSlotType equipmentSlotType, PlayerEntity player, Attribute attribute, AttributeModifier modifier);
    }

    public static final DecimalFormat DECIMALFORMAT = Util.make(new DecimalFormat("#.##"), format -> {
        format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    });

    static Map<Attribute, ItemAttributeRenderer> attributeRendererMap = new IdentityHashMap<>(60);

    public static void registerAttributeRenderer(Attribute attribute, ItemAttributeRenderer renderer) {
        attributeRendererMap.put(attribute, renderer);
    }

    static List<ITextComponent> renderAttribute(ItemStack stack, EquipmentSlotType equipmentSlotType,
                                                PlayerEntity player, Attribute attribute,
                                                AttributeModifier modifier) {
        return attributeRendererMap.getOrDefault(attribute, AttributeTooltipManager::defaultAttributeRender)
                .render(stack, equipmentSlotType, player, attribute, modifier);
    }

    static List<ITextComponent> defaultAttributeRender(ItemStack stack, EquipmentSlotType equipmentSlotType,
                                                       PlayerEntity player, Attribute attribute,
                                                       AttributeModifier modifier) {
        double amount = modifier.getAmount();
        boolean absolute = false;
        if (player != null) {
            if (modifier.getID().equals(ATTACK_DAMAGE_MODIFIER)) {
                amount = amount + player.getBaseAttributeValue(Attributes.ATTACK_DAMAGE);
                amount = amount + EnchantmentHelper.getModifierForCreature(stack, CreatureAttribute.UNDEFINED);
                absolute = true;
            } else if (modifier.getID().equals(ATTACK_SPEED_MODIFIER)) {
                amount += player.getBaseAttributeValue(Attributes.ATTACK_SPEED);
                absolute = true;
            }
        }

        double displayAmount;
        if (modifier.getOperation() == AttributeModifier.Operation.ADDITION) {
            if (attribute.equals(Attributes.KNOCKBACK_RESISTANCE)) {
                displayAmount = amount * 10.0D;
            } else {
                displayAmount = amount;
            }
        } else {
            displayAmount = amount * 100.0D;
        }

        if (absolute) {
            return Collections.singletonList(makeAbsoluteText(attribute, modifier, displayAmount));
        }

        ITextComponent line = makeBonusOrTakeText(attribute, modifier, amount, displayAmount);
        if (line != null) {
            return Collections.singletonList(line);
        }
        return Collections.emptyList();
    }

    @Nullable
    public static IFormattableTextComponent makeBonusOrTakeText(Attribute attribute, AttributeModifier modifier,
                                                                double amount, double displayAmount) {
        return makeBonusOrTakeText(attribute, modifier, amount, displayAmount, DECIMALFORMAT::format);
    }

    @Nullable
    public static IFormattableTextComponent makeBonusOrTakeText(Attribute attribute, AttributeModifier modifier,
                                                                double amount, double displayAmount, DoubleFunction<String> formatter) {
        if (amount > 0.0D) {
            return makeBonusText(attribute, modifier, displayAmount, formatter);
        } else if (amount < 0.0D) {
            return makeTakeText(attribute, modifier, displayAmount, formatter);
        }
        return null;
    }

    public static IFormattableTextComponent makeTakeText(Attribute attribute, AttributeModifier attributemodifier,
                                                         double displayAmount) {
        return makeTakeText(attribute, attributemodifier, displayAmount, DECIMALFORMAT::format);
    }

    @Nonnull
    public static IFormattableTextComponent makeTakeText(Attribute attribute, AttributeModifier modifier,
                                                         double displayAmount, DoubleFunction<String> formatter) {
        displayAmount = displayAmount * -1.0D;
        return new TranslationTextComponent("attribute.modifier.take." + modifier.getOperation().getId(),
                formatter.apply(displayAmount),
                new TranslationTextComponent(attribute.getAttributeName()))
                .mergeStyle(TextFormatting.RED);
    }

    public static IFormattableTextComponent makeBonusText(Attribute attribute, AttributeModifier modifier,
                                                          double displayAmount) {
        return makeBonusText(attribute, modifier, displayAmount, DECIMALFORMAT::format);
    }

    @Nonnull
    public static IFormattableTextComponent makeBonusText(Attribute attribute, AttributeModifier modifier,
                                                          double displayAmount, DoubleFunction<String> formatter) {
        return new TranslationTextComponent("attribute.modifier.plus." + modifier.getOperation().getId(),
                formatter.apply(displayAmount),
                new TranslationTextComponent(attribute.getAttributeName()))
                .mergeStyle(TextFormatting.BLUE);
    }

    public static IFormattableTextComponent makeAbsoluteText(Attribute attribute, AttributeModifier modifier,
                                                             double displayAmount) {
        return makeAbsoluteText(attribute, modifier, displayAmount, DECIMALFORMAT::format);
    }

    @Nonnull
    public static IFormattableTextComponent makeAbsoluteText(Attribute attribute, AttributeModifier modifier,
                                                             double displayAmount, DoubleFunction<String> formatter) {
        return new StringTextComponent(" ")
                .appendSibling(new TranslationTextComponent(
                        "attribute.modifier.equals." + modifier.getOperation().getId(),
                        formatter.apply(displayAmount),
                        new TranslationTextComponent(attribute.getAttributeName()))
                )
                .mergeStyle(TextFormatting.DARK_GREEN);
    }


    public static void renderTooltip(List<ITextComponent> list, PlayerEntity player, ItemStack stack,
                                     EquipmentSlotType equipmentSlot) {
        Multimap<Attribute, AttributeModifier> multimap = stack.getAttributeModifiers(equipmentSlot);
        if (!multimap.isEmpty()) {
            list.add(StringTextComponent.EMPTY);
            list.add(new TranslationTextComponent("item.modifiers." + equipmentSlot.getName()).mergeStyle(TextFormatting.GRAY));

            Comparator<Map.Entry<Attribute, AttributeModifier>> comp = Comparator.comparing(attr -> attr.getKey().getAttributeName());

            multimap.entries().stream().sorted(comp).forEach(entry -> {
                list.addAll(renderAttribute(stack, equipmentSlot, player, entry.getKey(), entry.getValue()));
            });
        }
    }
}
