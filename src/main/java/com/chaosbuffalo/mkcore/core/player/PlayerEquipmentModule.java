package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.AbilitySlot;
import com.chaosbuffalo.mkcore.core.IMKAbilityProvider;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.item.ArmorClass;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public class PlayerEquipmentModule {
    private static final UUID[] UUID_BY_SLOT = new UUID[]{
            UUID.fromString("536049db-3699-4cff-831c-52fe99b24269"),
            UUID.fromString("75a8a55f-13de-400f-a823-444e71729fd5"),
            UUID.fromString("c787ae8b-6cc1-4b72-ac00-e047f5005c32"),
            UUID.fromString("d598564a-84be-46fe-ac46-3028c6e45dd1"),
            UUID.fromString("38e5df08-9bd6-446e-a75d-f0b2aa150a73"),
            UUID.fromString("9b444ef7-5020-483e-b355-7b975958634a")
    };

    private final MKPlayerData playerData;
    private MKAbility currentMainAbility = null;

    public PlayerEquipmentModule(MKPlayerData playerData) {
        this.playerData = playerData;
    }

    public void onEquipmentChange(EquipmentSlotType slot, ItemStack from, ItemStack to) {
//        MKCore.LOGGER.info("Equipment[{}] {} -> {}", slot, from, to);
        if (slot.getSlotType() == EquipmentSlotType.Group.ARMOR) {
            handleArmorChange(slot, from, to);
        } else if (slot == EquipmentSlotType.MAINHAND) {
            handleMainHandChange(to);
        }
    }

    private UUID getSlotUUID(EquipmentSlotType slot) {
        return UUID_BY_SLOT[slot.ordinal()];
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
        if (to.getItem() instanceof IMKAbilityProvider) {
            currentMainAbility = ((IMKAbilityProvider) to.getItem()).getAbility(to);
            if (currentMainAbility != null) {
                if (currentMainAbility.getType().fitsSlot(AbilitySlot.Item)) {
                    if (!playerData.getAbilities().knowsAbility(currentMainAbility.getAbilityId())) {
                        playerData.getAbilities().learnAbility(currentMainAbility);
                    }
                    playerData.getAbilityLoadout().getAbilityGroup(AbilitySlot.Item).setSlot(0, currentMainAbility.getAbilityId());
                } else {
                    MKCore.LOGGER.error("Cannot use ability {} provided by Item {} because it uses the wrong AbilitySlot", currentMainAbility, to);
                }
            }
        } else {
            if (currentMainAbility != null) {
                playerData.getAbilityLoadout().getAbilityGroup(AbilitySlot.Item).clearSlot(0);
            }
            currentMainAbility = null;
        }
    }

    private void addArmorSlot(EquipmentSlotType slot, ItemStack to) {
        ArmorClass armorClass = ArmorClass.getItemArmorClass((ArmorItem) to.getItem());
        if (armorClass != null) {
            armorClass.getPositiveModifierMap(slot).forEach((attr, mod) -> {
                AttributeModifier dup = createSlotModifier(slot, mod);
                playerData.getEntity().getAttribute(attr).applyNonPersistentModifier(dup);
            });
            armorClass.getNegativeModifierMap(slot).forEach((attr, mod) -> {
                AttributeModifier dup = createSlotModifier(slot, mod);
                playerData.getEntity().getAttribute(attr).applyNonPersistentModifier(dup);
            });
        }
        addAbilityItem(to);
    }

    private void removeArmorSlot(EquipmentSlotType slot, ItemStack from) {
        ArmorClass itemClass = ArmorClass.getItemArmorClass((ArmorItem) from.getItem());
        if (itemClass != null) {
            UUID uuid = getSlotUUID(slot);
            itemClass.getPositiveModifierMap(slot).keySet()
                    .forEach(attr -> playerData.getEntity().getAttribute(attr).removeModifier(uuid));
            itemClass.getNegativeModifierMap(slot).keySet()
                    .forEach(attr -> playerData.getEntity().getAttribute(attr).removeModifier(uuid));
        }
        removeAbilityItem(from);
    }

    private AttributeModifier createSlotModifier(EquipmentSlotType slot, AttributeModifier mod) {
        return new AttributeModifier(getSlotUUID(slot), mod::getName, mod.getAmount(), mod.getOperation());
    }

    private void addAbilityItem(ItemStack newItem) {
        if (newItem.isEmpty())
            return;

        if (newItem.getItem() instanceof IMKAbilityProvider) {
            MKAbility ability = ((IMKAbilityProvider) newItem.getItem()).getAbility(newItem);
            if (ability != null) {
                playerData.getAbilities().learnAbility(ability);
            }
        }
    }

    private void removeAbilityItem(ItemStack oldItem) {
        if (oldItem.isEmpty())
            return;

        if (oldItem.getItem() instanceof IMKAbilityProvider) {
            MKAbility ability = ((IMKAbilityProvider) oldItem.getItem()).getAbility(oldItem);
            if (ability != null) {
                playerData.getAbilities().unlearnAbility(ability.getAbilityId());
            }
        }
    }

    public void onPersonaActivated() {
        PlayerEntity player = playerData.getEntity();
        handleMainHandChange(playerData.getEntity().getItemStackFromSlot(EquipmentSlotType.MAINHAND));
        addAbilityItem(player.getItemStackFromSlot(EquipmentSlotType.HEAD));
        addAbilityItem(player.getItemStackFromSlot(EquipmentSlotType.CHEST));
        addAbilityItem(player.getItemStackFromSlot(EquipmentSlotType.LEGS));
        addAbilityItem(player.getItemStackFromSlot(EquipmentSlotType.FEET));
    }

    public void onPersonaDeactivated() {
        PlayerEntity player = playerData.getEntity();
        removeAbilityItem(player.getItemStackFromSlot(EquipmentSlotType.HEAD));
        removeAbilityItem(player.getItemStackFromSlot(EquipmentSlotType.CHEST));
        removeAbilityItem(player.getItemStackFromSlot(EquipmentSlotType.LEGS));
        removeAbilityItem(player.getItemStackFromSlot(EquipmentSlotType.FEET));
    }
}
