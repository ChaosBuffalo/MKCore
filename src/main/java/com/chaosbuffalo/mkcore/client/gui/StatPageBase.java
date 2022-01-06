package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import java.util.Collection;

public abstract class StatPageBase extends PlayerPageBase {
    protected static final int NEGATIVE_COLOR = 13111115;
    protected static final int POSITIVE_COLOR = 3334475;
    protected static final int BASE_COLOR = 16777215;

    public StatPageBase(MKPlayerData playerData, ITextComponent title) {
        super(playerData, title);
    }

    protected MKText getTextForAttribute(MKPlayerData playerData, Attribute attr) {
        ModifiableAttributeInstance attribute = playerData.getEntity().getAttribute(attr);
        String text = String.format("%s: %.2f", I18n.format(attr.getAttributeName()), attribute.getValue());
        MKText textWidget = new MKText(minecraft.fontRenderer, text).setMultiline(true);
        addPreDrawRunnable(() -> {
            String newText = String.format("%s: %.2f", I18n.format(attr.getAttributeName()), attribute.getValue());
            textWidget.setText(newText);
            double baseValue = attribute.getBaseValue();
            if (attr.equals(Attributes.ATTACK_SPEED) && minecraft.player != null) {
                ItemStack itemInHand = minecraft.player.getHeldItemMainhand();
                if (!itemInHand.equals(ItemStack.EMPTY)) {
                    if (itemInHand.getAttributeModifiers(EquipmentSlotType.MAINHAND).containsKey(attr)) {
                        Collection<AttributeModifier> itemAttackSpeed = itemInHand.getAttributeModifiers(EquipmentSlotType.MAINHAND)
                                .get(attr);
                        double attackSpeed = 4.0;
                        for (AttributeModifier mod : itemAttackSpeed) {
                            if (mod.getOperation().equals(AttributeModifier.Operation.ADDITION)) {
                                attackSpeed += mod.getAmount();
                            }
                        }
                        baseValue = attackSpeed;
                    }
                }
            }
            if (attribute.getValue() < baseValue) {
                textWidget.setColor(NEGATIVE_COLOR);
            } else if (attribute.getValue() > baseValue) {
                textWidget.setColor(POSITIVE_COLOR);
            } else {
                textWidget.setColor(BASE_COLOR);
            }
        });
        return textWidget;
    }
}
