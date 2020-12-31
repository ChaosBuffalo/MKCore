package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.abilities.AbilityManager;
import com.chaosbuffalo.mkcore.client.gui.MKOverlay;
import com.chaosbuffalo.mkcore.client.rendering.MKRenderers;
import com.chaosbuffalo.mkcore.command.MKCommand;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.persona.IPersonaExtensionProvider;
import com.chaosbuffalo.mkcore.core.persona.PersonaManager;
import com.chaosbuffalo.mkcore.core.talents.TalentManager;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MKCore.MOD_ID)
public class MKCore {
    public static final String MOD_ID = "mkcore";
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    private final AbilityManager abilityManager;
    private final TalentManager talentManager;

    public static MKCore INSTANCE;

    public MKCore() {
        INSTANCE = this;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        talentManager = new TalentManager();
        abilityManager = new AbilityManager();

        MKConfig.init();
        // REMOVEME when MKFaction ready
        Targeting.registerRelationCallback((caster, target) -> Targeting.TargetRelation.ENEMY);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        PacketHandler.setupHandler();
        CoreCapabilities.registerCapabilities();
        MKCommand.registerArguments();
    }

    private void loadComplete(final FMLLoadCompleteEvent event) {
        // Hopefully other mods will have put their entries in the GlobalEntityTypeAttributes by now
        registerAttributes();
    }

    private void registerAttributes() {
        Attributes.ATTACK_DAMAGE.setShouldWatch(true);

        AttributeFixer.addAttributesToAll(builder ->
                MKAttributes.registerEntityAttributes(builder::createMutableAttribute));
        AttributeFixer.addAttributes(EntityType.PLAYER, builder ->
                MKAttributes.registerPlayerAttributes(builder::createMutableAttribute));

        GlobalEntityTypeAttributes.getAttributesForEntity(EntityType.PLAYER).attributeMap.forEach(((attribute, modifiableAttributeInstance) -> {
            if (!ForgeRegistries.ATTRIBUTES.containsKey(attribute.getRegistryName())) {
                MKCore.LOGGER.error("ERROR: Player attribute {} was not registered with the registry!", attribute);
            }
        }));
    }

    @SubscribeEvent
    public void serverStart(FMLServerAboutToStartEvent event) {
        // some preinit code
        LOGGER.info("HELLO FROM ABOUTTOSTART");
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
        MinecraftForge.EVENT_BUS.register(new MKOverlay());
        ClientEventHandler.initKeybindings();
        MKRenderers.registerPlayerRenderers();
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        MKCommand.registerCommands(event.getDispatcher());
    }

    @SubscribeEvent
    public void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(abilityManager);
        event.addListener(talentManager);
    }

    private void processIMC(final InterModProcessEvent event) {
        MKCore.LOGGER.info("MKCore.processIMC");
        event.getIMCStream().forEach(m -> {
            if (m.getMethod().equals("register_persona_extension")) {
                MKCore.LOGGER.info("IMC register persona extension from mod {} {}", m.getSenderModId(), m.getMethod());
                IPersonaExtensionProvider factory = (IPersonaExtensionProvider) m.getMessageSupplier().get();
                PersonaManager.registerExtension(factory);
            }
        });
    }

    public static ResourceLocation makeRL(String path) {
        return new ResourceLocation(MKCore.MOD_ID, path);
    }

    public static LazyOptional<MKPlayerData> getPlayer(Entity playerEntity) {
        return playerEntity.getCapability(CoreCapabilities.PLAYER_CAPABILITY);
    }

    public static LazyOptional<? extends IMKEntityData> getEntityData(Entity entity) {
        if (entity instanceof PlayerEntity) {
            return entity.getCapability(CoreCapabilities.PLAYER_CAPABILITY);
        } else {
            return entity.getCapability(CoreCapabilities.ENTITY_CAPABILITY);
        }
    }

    public static TalentManager getTalentManager() {
        return INSTANCE.talentManager;
    }

    public static AbilityManager getAbilityManager() {
        return INSTANCE.abilityManager;
    }

    static class AttributeFixer {
        public static void addAttributes(EntityType<? extends LivingEntity> type, Consumer<AttributeModifierMap.MutableAttribute> builder) {
            Map<Attribute, ModifiableAttributeInstance> finalMap;
            if (GlobalEntityTypeAttributes.doesEntityHaveAttributes(type)) {
                finalMap = new HashMap<>(GlobalEntityTypeAttributes.getAttributesForEntity(type).attributeMap);
            } else {
                finalMap = new HashMap<>();
            }

            AttributeModifierMap.MutableAttribute newAttrs = AttributeModifierMap.createMutableAttribute();
            builder.accept(newAttrs);

            finalMap.putAll(newAttrs.create().attributeMap);
            GlobalEntityTypeAttributes.put(type, new AttributeModifierMap(finalMap));
        }

        public static void addAttributesToAll(Consumer<AttributeModifierMap.MutableAttribute> builder) {
            ForgeRegistries.ENTITIES.forEach(entityType -> {
                if (GlobalEntityTypeAttributes.doesEntityHaveAttributes(entityType)) {
                    LOGGER.info("Adding attributes to {}", entityType);
                    addAttributes((EntityType<? extends LivingEntity>) entityType, builder);
                }
            });
        }
    }
}
