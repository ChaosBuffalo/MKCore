package com.chaosbuffalo.mkcore.item;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.init.CoreTags;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.*;


public class ArmorClass {

    public static final ArmorClass LIGHT = new ArmorClass(MKCore.makeRL("armor_class.light"), CoreTags.Items.LIGHT_ARMOR)
            .addPositiveEffect(Attributes.MOVEMENT_SPEED, 0.025, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(MKAttributes.CASTING_SPEED, 0.025, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(MKAttributes.MANA_REGEN, 0.025, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addNegativeEffect(Attributes.ARMOR, -0.04, AttributeModifier.Operation.MULTIPLY_TOTAL);
    public static final ArmorClass MEDIUM = new ArmorClass(MKCore.makeRL("armor_class.medium"), CoreTags.Items.MEDIUM_ARMOR)
            .addPositiveEffect(MKAttributes.MELEE_CRIT, 0.03, AttributeModifier.Operation.ADDITION)
            .addPositiveEffect(Attributes.ATTACK_SPEED, 0.03, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addNegativeEffect(MKAttributes.COOLDOWN, -0.02, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addNegativeEffect(MKAttributes.CASTING_SPEED, -0.02, AttributeModifier.Operation.MULTIPLY_TOTAL);
    public static final ArmorClass HEAVY = new ArmorClass(MKCore.makeRL("armor_class.heavy"), CoreTags.Items.HEAVY_ARMOR)
            .addPositiveEffect(Attributes.ATTACK_DAMAGE, 0.025, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(MKAttributes.ARCANE_RESISTANCE, 0.015, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(MKAttributes.FIRE_RESISTANCE, 0.015, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(MKAttributes.FROST_RESISTANCE, 0.015, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(MKAttributes.NATURE_RESISTANCE, 0.015, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(MKAttributes.POISON_RESISTANCE, 0.015, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addPositiveEffect(Attributes.ARMOR_TOUGHNESS, 0.025, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addNegativeEffect(Attributes.MOVEMENT_SPEED, -0.025, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addNegativeEffect(MKAttributes.COOLDOWN, -0.04, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addNegativeEffect(MKAttributes.CASTING_SPEED, -0.04, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addNegativeEffect(Attributes.ATTACK_SPEED, -0.025, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final List<ArmorClass> CHECK_ORDER = Arrays.asList(LIGHT, MEDIUM, HEAVY);

    private final ResourceLocation location;
    private final Map<Attribute, AttributeModifier> positiveModifierMap = new HashMap<>();
    private final Map<Attribute, AttributeModifier> negativeModifierMap = new HashMap<>();
    private final Set<IArmorMaterial> materials = new HashSet<>();
    private final ITag.INamedTag<Item> tag;

    private static ArmorClass getArmorClassForMaterial(IArmorMaterial material) {
        return CHECK_ORDER.stream()
                .filter(armorClass -> armorClass.hasMaterial(material))
                .findFirst()
                .orElse(null);
    }

    public static ArmorClass getItemArmorClass(ArmorItem item) {
        return CHECK_ORDER.stream()
                .filter(armorClass -> armorClass.containsItem(item))
                .findFirst()
                .orElseGet(() -> getArmorClassForMaterial(item.getArmorMaterial()));
    }

    public ArmorClass(ResourceLocation location, ITag.INamedTag<Item> tag) {
        this.location = location;
        this.tag = tag;
    }

    public ArmorClass addNegativeEffect(Attribute attributeIn, double amount, AttributeModifier.Operation operation) {
        AttributeModifier attributemodifier = new AttributeModifier(getTranslationKey(), amount, operation);
        this.negativeModifierMap.put(attributeIn, attributemodifier);
        return this;
    }

    public ArmorClass addPositiveEffect(Attribute attributeIn, double amount, AttributeModifier.Operation operation) {
        AttributeModifier attributemodifier = new AttributeModifier(getTranslationKey(), amount, operation);
        this.positiveModifierMap.put(attributeIn, attributemodifier);
        return this;
    }

    public Map<Attribute, AttributeModifier> getPositiveModifierMap(EquipmentSlotType slot) {
        return this.positiveModifierMap;
    }

    public Map<Attribute, AttributeModifier> getNegativeModifierMap(EquipmentSlotType slot) {
        return this.negativeModifierMap;
    }

    private String getTranslationKey() {
        return String.format("%s.%s.name", location.getNamespace(), location.getPath());
    }

    public ITextComponent getName() {
        return new TranslationTextComponent(getTranslationKey());
    }

    public ResourceLocation getLocation() {
        return location;
    }

    private boolean hasMaterial(IArmorMaterial material) {
        return materials.contains(material);
    }

    private boolean containsItem(ArmorItem item) {
        return tag == null || tag.contains(item);
    }

    public ArmorClass register(IArmorMaterial material) {
        materials.add(material);
        return this;
    }
}
