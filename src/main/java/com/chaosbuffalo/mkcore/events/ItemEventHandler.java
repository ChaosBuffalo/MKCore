package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.CoreCapabilities;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import com.chaosbuffalo.mkcore.item.IImplementsBlocking;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ItemEventHandler {
    private static final UUID SHIELD_EFFICIENCY_MOD_MAINHAND = UUID.fromString("ef26b7ab-f309-4bf6-9b9f-928173c467f1");
    private static final UUID SHIELD_POISE_MOD_MAINHAND = UUID.fromString("b463e341-5c71-4855-966e-a6aa2743d22f");
    private static final UUID SHIELD_EFFICIENCY_MOD_OFFHAND = UUID.fromString("2a83c9cc-ee55-4270-a842-1eb56969d335");
    private static final UUID SHIELD_POISE_MOD_OFFHAND = UUID.fromString("94fe11b3-5b65-47f1-ad76-93ba2cd15b6a");
    private static final UUID SWORD_EFFICIENCY_MOD_MAINHAND = UUID.fromString("5dabae27-f1a6-4b45-b63e-c6acd8b356a4");
    private static final UUID SWORD_POISE_MOD_MAINHAND = UUID.fromString("8b45f437-5758-482d-80c3-ceddb13d9fe4");
    private static final UUID SWORD_EFFICIENCY_MOD_OFFHAND = UUID.fromString("07c3acc2-82df-4873-8444-d09260e08594");
    private static final UUID SWORD_POISE_MOD_OFFHAND = UUID.fromString("e5a445c0-a08d-4cf5-960a-0945c505da94");

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntityLiving() instanceof ServerPlayerEntity))
            return;

        ServerPlayerEntity player = (ServerPlayerEntity) event.getEntityLiving();
        player.getCapability(CoreCapabilities.PLAYER_CAPABILITY).ifPresent((playerData) -> {
            playerData.getEquipment().onEquipmentChange(event.getSlot(), event.getFrom(), event.getTo());
            SpellTriggers.PLAYER_EQUIPMENT_CHANGE.onEquipmentChange(event, playerData, player);
        });


        Item from = event.getFrom().getItem();
        Item to = event.getTo().getItem();
        if (from instanceof ShieldItem){
            event.getEntityLiving().getAttribute(MKAttributes.BLOCK_EFFICIENCY).removeModifier(
                    event.getSlot() == EquipmentSlotType.MAINHAND ? SHIELD_EFFICIENCY_MOD_MAINHAND : SHIELD_EFFICIENCY_MOD_OFFHAND);
            event.getEntityLiving().getAttribute(MKAttributes.MAX_POISE).removeModifier(
                    event.getSlot() == EquipmentSlotType.MAINHAND ? SHIELD_POISE_MOD_MAINHAND : SHIELD_POISE_MOD_OFFHAND);
        }
        if (to instanceof ShieldItem){
            event.getEntityLiving().getAttribute(MKAttributes.BLOCK_EFFICIENCY)
                    .applyNonPersistentModifier(new AttributeModifier(event.getSlot() == EquipmentSlotType.MAINHAND ? SHIELD_EFFICIENCY_MOD_MAINHAND : SHIELD_EFFICIENCY_MOD_OFFHAND,
                            "Shield Modifier", 1.0, AttributeModifier.Operation.ADDITION));
            event.getEntityLiving().getAttribute(MKAttributes.MAX_POISE)
                    .applyNonPersistentModifier(new AttributeModifier(event.getSlot() == EquipmentSlotType.MAINHAND ? SHIELD_POISE_MOD_MAINHAND : SHIELD_POISE_MOD_OFFHAND,
                            "Shield Modifier", 50.0, AttributeModifier.Operation.ADDITION));
        }
        if (from instanceof SwordItem && !(from instanceof IImplementsBlocking)){
            event.getEntityLiving().getAttribute(MKAttributes.BLOCK_EFFICIENCY).removeModifier(
                    event.getSlot() == EquipmentSlotType.MAINHAND ? SWORD_EFFICIENCY_MOD_MAINHAND : SWORD_EFFICIENCY_MOD_OFFHAND);
            event.getEntityLiving().getAttribute(MKAttributes.MAX_POISE).removeModifier(
                    event.getSlot() == EquipmentSlotType.MAINHAND ? SWORD_POISE_MOD_MAINHAND : SWORD_POISE_MOD_OFFHAND);
        }
        if (to instanceof SwordItem && !(to instanceof IImplementsBlocking)) {
            event.getEntityLiving().getAttribute(MKAttributes.BLOCK_EFFICIENCY)
                    .applyNonPersistentModifier(new AttributeModifier(event.getSlot() == EquipmentSlotType.MAINHAND ? SWORD_EFFICIENCY_MOD_MAINHAND : SWORD_EFFICIENCY_MOD_OFFHAND,
                            "Sword Modifier", 0.75, AttributeModifier.Operation.ADDITION));
            event.getEntityLiving().getAttribute(MKAttributes.MAX_POISE)
                    .applyNonPersistentModifier(new AttributeModifier(event.getSlot() == EquipmentSlotType.MAINHAND ? SWORD_POISE_MOD_MAINHAND : SWORD_POISE_MOD_OFFHAND,
                            "Sword Modifier", 20.0, AttributeModifier.Operation.ADDITION));
        }
    }
}
