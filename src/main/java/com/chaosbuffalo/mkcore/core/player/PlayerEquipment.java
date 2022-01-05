package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.IMKAbilityProvider;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.item.ArmorClass;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public class PlayerEquipment {
    private static final UUID[] ARMOR_CLASS_UUID_BY_SLOT = new UUID[]{
            UUID.fromString("536049db-3699-4cff-831c-52fe99b24269"),
            UUID.fromString("75a8a55f-13de-400f-a823-444e71729fd5"),
            UUID.fromString("c787ae8b-6cc1-4b72-ac00-e047f5005c32"),
            UUID.fromString("d598564a-84be-46fe-ac46-3028c6e45dd1"),
            UUID.fromString("38e5df08-9bd6-446e-a75d-f0b2aa150a73"),
            UUID.fromString("9b444ef7-5020-483e-b355-7b975958634a")
    };

    private final MKPlayerData playerData;
    private MKAbility currentMainAbility = null;

    public PlayerEquipment(MKPlayerData playerData) {
        this.playerData = playerData;
    }

    public void onEquipmentChange(EquipmentSlotType slot, ItemStack from, ItemStack to) {
        // Currently, we only care about swapping items so modifications like durability are ignored
        if (ItemStack.areItemStacksEqual(from, to))
            return;

//        MKCore.LOGGER.info("Equipment[{}] {} -> {}", slot, from, to);
        if (slot.getSlotType() == EquipmentSlotType.Group.ARMOR) {
            handleArmorChange(slot, from, to);
        } else if (slot == EquipmentSlotType.MAINHAND) {
            handleMainHandChange(to);
        }
    }

    private void handleArmorChange(EquipmentSlotType slot, ItemStack from, ItemStack to) {
        if (!from.isEmpty() && from.getItem() instanceof ArmorItem) {
            removeArmorSlot(slot, from);
        }
        if (!to.isEmpty() && to.getItem() instanceof ArmorItem) {
            addArmorSlot(slot, to);
        }
    }

    private void handleMainHandChange(ItemStack to) {
        // Clear the current ability if present
        if (currentMainAbility != null) {
            playerData.getLoadout().getAbilityGroup(AbilityGroupId.Item).clearSlot(0);
            currentMainAbility = null;
        }

        if (to.getItem() instanceof IMKAbilityProvider) {
            currentMainAbility = ((IMKAbilityProvider) to.getItem()).getAbility(to);
            if (currentMainAbility != null) {
                playerData.getLoadout().getAbilityGroup(AbilityGroupId.Item).setSlot(0, currentMainAbility.getAbilityId());
            }
        }
    }

    private void addArmorSlot(EquipmentSlotType slot, ItemStack to) {
        applyArmorClassBonus(slot, to);
        addItemAbility(to);
    }

    private void removeArmorSlot(EquipmentSlotType slot, ItemStack from) {
        removeArmorClassBonus(slot, from);
        removeItemAbility(from);
    }

    private UUID getArmorClassSlotUUID(EquipmentSlotType slot) {
        return ARMOR_CLASS_UUID_BY_SLOT[slot.ordinal()];
    }

    private void applyArmorClassBonus(EquipmentSlotType slot, ItemStack to) {
        ArmorClass armorClass = ArmorClass.getItemArmorClass((ArmorItem) to.getItem());
        if (armorClass != null) {
            armorClass.getPositiveModifierMap(slot).forEach((attr, mod) -> {
                AttributeModifier dup = createArmorClassSlotModifier(slot, mod);
                playerData.getEntity().getAttribute(attr).applyNonPersistentModifier(dup);
            });
            armorClass.getNegativeModifierMap(slot).forEach((attr, mod) -> {
                AttributeModifier dup = createArmorClassSlotModifier(slot, mod);
                playerData.getEntity().getAttribute(attr).applyNonPersistentModifier(dup);
            });
        }
    }

    private void removeArmorClassBonus(EquipmentSlotType slot, ItemStack from) {
        ArmorClass itemClass = ArmorClass.getItemArmorClass((ArmorItem) from.getItem());
        if (itemClass != null) {
            UUID uuid = getArmorClassSlotUUID(slot);
            itemClass.getPositiveModifierMap(slot).keySet()
                    .forEach(attr -> playerData.getEntity().getAttribute(attr).removeModifier(uuid));
            itemClass.getNegativeModifierMap(slot).keySet()
                    .forEach(attr -> playerData.getEntity().getAttribute(attr).removeModifier(uuid));
        }
    }

    private AttributeModifier createArmorClassSlotModifier(EquipmentSlotType slot, AttributeModifier template) {
        return new AttributeModifier(getArmorClassSlotUUID(slot), template::getName, template.getAmount(), template.getOperation());
    }

    private void addItemAbility(ItemStack newItem) {
        if (newItem.isEmpty())
            return;

        if (newItem.getItem() instanceof IMKAbilityProvider) {
            MKAbility ability = ((IMKAbilityProvider) newItem.getItem()).getAbility(newItem);
            if (ability != null) {
                playerData.getAbilities().learnAbility(ability, AbilitySource.forItem(newItem));
            }
        }
    }

    private void removeItemAbility(ItemStack oldItem) {
        if (oldItem.isEmpty())
            return;

        if (oldItem.getItem() instanceof IMKAbilityProvider) {
            MKAbility ability = ((IMKAbilityProvider) oldItem.getItem()).getAbility(oldItem);
            if (ability != null) {
                playerData.getAbilities().unlearnAbility(ability.getAbilityId(), AbilitySource.forItem(oldItem));
            }
        }
    }

    public void onPersonaActivated() {
        PlayerEntity player = playerData.getEntity();
        ItemStack mainHand = player.getItemStackFromSlot(EquipmentSlotType.MAINHAND);
        handleMainHandChange(mainHand);
        addItemAbility(player.getItemStackFromSlot(EquipmentSlotType.HEAD));
        addItemAbility(player.getItemStackFromSlot(EquipmentSlotType.CHEST));
        addItemAbility(player.getItemStackFromSlot(EquipmentSlotType.LEGS));
        addItemAbility(player.getItemStackFromSlot(EquipmentSlotType.FEET));
    }

    public void onPersonaDeactivated() {
        PlayerEntity player = playerData.getEntity();
        handleMainHandChange(ItemStack.EMPTY);
        removeItemAbility(player.getItemStackFromSlot(EquipmentSlotType.HEAD));
        removeItemAbility(player.getItemStackFromSlot(EquipmentSlotType.CHEST));
        removeItemAbility(player.getItemStackFromSlot(EquipmentSlotType.LEGS));
        removeItemAbility(player.getItemStackFromSlot(EquipmentSlotType.FEET));
    }
}
