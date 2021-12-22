package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.client.gui.CharacterScreen;
import com.chaosbuffalo.mkcore.client.gui.IPlayerDataAwareScreen;
import com.chaosbuffalo.mkcore.client.gui.ParticleEditorScreen;
import com.chaosbuffalo.mkcore.core.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKRangedAttribute;
import com.chaosbuffalo.mkcore.effects.status.StunEffect;
import com.chaosbuffalo.mkcore.events.PlayerDataEvent;
import com.chaosbuffalo.mkcore.events.PostAttackEvent;
import com.chaosbuffalo.mkcore.item.ArmorClass;
import com.chaosbuffalo.mkcore.network.ExecuteActiveAbilityPacket;
import com.chaosbuffalo.mkcore.network.MKItemAttackPacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.utils.RayTraceUtils;
import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, value = Dist.CLIENT)
public class ClientEventHandler {

    private static KeyBinding playerMenuBind;
    private static KeyBinding particleEditorBind;
    private static KeyBinding[] activeAbilityBinds;
    private static KeyBinding[] ultimateAbilityBinds;
    private static KeyBinding itemAbilityBind;

    private static int currentGCDTicks;

    public static void initKeybindings() {
        playerMenuBind = new KeyBinding("key.hud.playermenu", GLFW.GLFW_KEY_J, "key.mkcore.category");
        ClientRegistry.registerKeyBinding(playerMenuBind);

        particleEditorBind = new KeyBinding("key.hud.particle_editor", GLFW.GLFW_KEY_KP_ADD, "key.mkcore.category");
        ClientRegistry.registerKeyBinding(particleEditorBind);

        activeAbilityBinds = new KeyBinding[GameConstants.MAX_BASIC_ABILITIES];
        for (int i = 0; i < GameConstants.MAX_BASIC_ABILITIES; i++) {
            String bindName = String.format("key.hud.active_ability%d", i + 1);
            int key = GLFW.GLFW_KEY_1 + i;
            KeyBinding bind = new KeyBinding(bindName, KeyConflictContext.IN_GAME, KeyModifier.ALT,
                    InputMappings.getInputByCode(key, 0), "key.mkcore.abilitybar");

            ClientRegistry.registerKeyBinding(bind);
            activeAbilityBinds[i] = bind;
        }

        ultimateAbilityBinds = new KeyBinding[GameConstants.MAX_ULTIMATE_ABILITIES];
        for (int i = 0; i < GameConstants.MAX_ULTIMATE_ABILITIES; i++) {
            String bindName = String.format("key.hud.ultimate_ability%d", i + 1);
            int key = GLFW.GLFW_KEY_6 + i;
            KeyBinding bind = new KeyBinding(bindName, KeyConflictContext.IN_GAME, KeyModifier.ALT,
                    InputMappings.getInputByCode(key, 0), "key.mkcore.abilitybar");

            ClientRegistry.registerKeyBinding(bind);
            ultimateAbilityBinds[i] = bind;
        }


        int defaultItemKey = GLFW.GLFW_KEY_8;
        itemAbilityBind = new KeyBinding("key.hud.item_ability", KeyConflictContext.IN_GAME, KeyModifier.ALT,
                InputMappings.getInputByCode(defaultItemKey, 0), "key.mkcore.abilitybar");
        ClientRegistry.registerKeyBinding(itemAbilityBind);
    }

    public static float getGlobalCooldown() {
        return (float) currentGCDTicks / GameConstants.TICKS_PER_SECOND;
    }

    public static float getTotalGlobalCooldown() {
        return (float) GameConstants.GLOBAL_COOLDOWN_TICKS / GameConstants.TICKS_PER_SECOND;
    }

    static boolean isOnGlobalCooldown() {
        return currentGCDTicks > 0;
    }

    static void startGlobalCooldown() {
        currentGCDTicks = GameConstants.GLOBAL_COOLDOWN_TICKS;
    }

    @SubscribeEvent
    public static void onKeyEvent(InputEvent.KeyInputEvent event) {
        handleInputEvent();
    }

    @SubscribeEvent
    public static void onMouseEvent(InputEvent.MouseInputEvent event) {
        handleInputEvent();
    }

    @SubscribeEvent
    public static void onRawMouseEvent(InputEvent.RawMouseEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null &&
                minecraft.player.isPotionActive(StunEffect.INSTANCE) &&
                minecraft.currentScreen == null) {
            event.setCanceled(true);
        }
    }

    static void handleAbilityBarPressed(PlayerEntity player, AbilityGroupId group, int slot) {
        if (isOnGlobalCooldown() || player.isPotionActive(StunEffect.INSTANCE))
            return;

        MKCore.getPlayer(player).ifPresent(pData -> {
            ResourceLocation abilityId = pData.getLoadout().getAbilityInSlot(group, slot);
            if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY))
                return;

            MKAbility ability = MKCoreRegistry.getAbility(abilityId);

            if (ability != null && pData.getAbilityExecutor().clientSimulateAbility(ability, group)) {
                MKCore.LOGGER.info("sending execute ability {} {}", group, slot);
                PacketHandler.sendMessageToServer(new ExecuteActiveAbilityPacket(group, slot));
                startGlobalCooldown();
            }
        });
    }

    public static void handleInputEvent() {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null)
            return;

        while (playerMenuBind.isPressed()) {
            Minecraft.getInstance().displayGuiScreen(new CharacterScreen());
        }

        while (particleEditorBind.isPressed()) {
            Minecraft.getInstance().displayGuiScreen(new ParticleEditorScreen());
        }

        for (int i = 0; i < activeAbilityBinds.length; i++) {
            KeyBinding bind = activeAbilityBinds[i];
            while (bind.isPressed()) {
                handleAbilityBarPressed(player, AbilityGroupId.Basic, i);
            }
        }

        for (int i = 0; i < ultimateAbilityBinds.length; i++) {
            KeyBinding bind = ultimateAbilityBinds[i];
            while (bind.isPressed()) {
                handleAbilityBarPressed(player, AbilityGroupId.Ultimate, i);
            }
        }

        while (itemAbilityBind.isPressed()) {
            handleAbilityBarPressed(player, AbilityGroupId.Item, 0);
        }
    }

    @SubscribeEvent
    public static void onTickEvent(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if (currentGCDTicks > 0) {
                currentGCDTicks--;
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDataUpdated(PlayerDataEvent.Updated event) {
        if (event.getPlayer().getEntityWorld().isRemote) {
            PlayerEntity local = Minecraft.getInstance().player;
            if (local == null || !event.getPlayer().isEntityEqual(local))
                return;

            if (Minecraft.getInstance().currentScreen instanceof IPlayerDataAwareScreen) {
                ((IPlayerDataAwareScreen) Minecraft.getInstance().currentScreen).onPlayerDataUpdate();
            }
        }
    }

    @SubscribeEvent
    public static void doArmorClassTooltip(ItemTooltipEvent event) {
        // Don't do anything during the initial search tree population
        if (event.getPlayer() == null)
            return;
        if (event.getItemStack().getItem() instanceof ShieldItem){
            event.getToolTip().add(new TranslationTextComponent("mkcore.max_poise.description",
                    50).mergeStyle(TextFormatting.GRAY));
            event.getToolTip().add(new TranslationTextComponent("mkcore.block_efficiency.description",
                    1.0).mergeStyle(TextFormatting.GRAY));
        }
        if (!MKConfig.CLIENT.showArmorClassOnTooltip.get())
            return;

        if (event.getItemStack().getItem() instanceof ArmorItem) {
            ArmorItem armorItem = (ArmorItem) event.getItemStack().getItem();
            ArmorClass armorClass = ArmorClass.getItemArmorClass(armorItem);
            if (armorClass == null) {
                return;
            }

            event.getToolTip().add(new TranslationTextComponent("mkcore.gui.item.armor_class.name")
                    .appendString(": ")
                    .appendSibling(armorClass.getName()));

            if (MKConfig.CLIENT.showArmorClassEffectsOnTooltip.get()) {
                List<ITextComponent> tooltip = event.getToolTip();
                if (Screen.hasShiftDown()) {
                    armorClass.getPositiveModifierMap(armorItem.getEquipmentSlot()).forEach(((attribute, modifier) -> {
                        addAttributeToTooltip(tooltip, attribute, modifier, TextFormatting.GREEN);
                    }));
                    armorClass.getNegativeModifierMap(armorItem.getEquipmentSlot()).forEach(((attribute, modifier) -> {
                        addAttributeToTooltip(tooltip, attribute, modifier, TextFormatting.RED);
                    }));
                } else {
                    tooltip.add(new TranslationTextComponent("mkcore.gui.item.armor_class.effect_prompt"));
                }
            }
        }

    }

    private static void addAttributeToTooltip(List<ITextComponent> tooltip, Attribute attribute,
                                              AttributeModifier modifier, TextFormatting color) {
        String suffix = "";
        double amount = modifier.getAmount();
        if (modifier.getOperation() == AttributeModifier.Operation.ADDITION) {
            if (attribute instanceof MKRangedAttribute) {
                if (((MKRangedAttribute) attribute).displayAdditionAsPercentage()) {
                    suffix = "%";
                    amount *= 100;
                }
            }
        }
        if (modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) {
            amount *= 100;
            suffix = "%";
        } else if (modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE) {
            amount *= 100;
            suffix = "% of base";
        }
        String prefix = amount > 0 ? "+" : "";

        ITextComponent component = new TranslationTextComponent("mkcore.gui.item.armor_class.effect.name")
                .mergeStyle(color)
                .appendString(String.format(": %s%.2f%s ", prefix, amount, suffix))
                .appendSibling(new TranslationTextComponent(attribute.getAttributeName()));

        tooltip.add(component);
    }

    private static void doPlayerAttack(PlayerEntity player, Entity target, Minecraft minecraft) {
        if (minecraft.playerController != null) {
            minecraft.playerController.syncCurrentPlayItem();
        }
        PacketHandler.sendMessageToServer(new MKItemAttackPacket(target));
        if (!player.isSpectator()) {
            player.attackTargetEntityWithCurrentItem(target);
            player.resetCooldown();
            MKCore.getEntityData(player).ifPresent(cap -> cap.getCombatExtension().recordSwing());
            MinecraftForge.EVENT_BUS.post(new PostAttackEvent(player));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAttackReplacement(InputEvent.ClickInputEvent event) {
        if (event.isAttack() && event.getHand() == Hand.MAIN_HAND) {
            PlayerEntity player = Minecraft.getInstance().player;
            if (player != null) {
                RayTraceResult lookingAt = RayTraceUtils.getLookingAt(Entity.class,
                        player, player.getAttribute(MKAttributes.ATTACK_REACH).getValue(),
                        (e) -> true);
                if (lookingAt != null && lookingAt.getType() == RayTraceResult.Type.ENTITY) {
                    EntityRayTraceResult traceResult = (EntityRayTraceResult) lookingAt;
                    Entity entityHit = traceResult.getEntity();
                    if (!Targeting.isValidFriendly(player, entityHit)) {
                        if (player.ticksSinceLastSwing > player.getCooldownPeriod()) {
                            doPlayerAttack(player, entityHit, Minecraft.getInstance());
                            event.setSwingHand(true);
                        }
                    }
                    event.setCanceled(true);
                }
            }
        }
    }
}
