package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.abilities.AbilityManager;
import com.chaosbuffalo.mkcore.client.gui.PlayerPageRegistry;
import com.chaosbuffalo.mkcore.client.gui.MKOverlay;
import com.chaosbuffalo.mkcore.client.rendering.MKRenderers;
import com.chaosbuffalo.mkcore.command.MKCommand;
import com.chaosbuffalo.mkcore.core.ICoreExtension;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.persona.IPersonaExtensionProvider;
import com.chaosbuffalo.mkcore.core.persona.PersonaManager;
import com.chaosbuffalo.mkcore.core.talents.TalentManager;
import com.chaosbuffalo.mkcore.events.ClientEventHandler;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.mkcore.init.CoreItems;
import com.chaosbuffalo.mkcore.init.CoreParticles;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.test.MKTestAbilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(MKCore.MOD_ID)
public class MKCore {
    public static final String MOD_ID = "mkcore";
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    private final AbilityManager abilityManager;
    private final TalentManager talentManager;
    private final ParticleAnimationManager particleAnimationManager;
    public static final String CORE_EXTENSION = "mk_core_extension";
    public static final String PERSONA_EXTENSION = "register_persona_extension";
    public static final String REGISTER_PLAYER_PAGE = "register_player_page";

    public static MKCore INSTANCE;

    public MKCore() {
        INSTANCE = this;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(EventPriority.LOWEST, this::loadComplete);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::modifyAttributesEvent);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        MKTestAbilities.register();
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        talentManager = new TalentManager();
        abilityManager = new AbilityManager();
        particleAnimationManager = new ParticleAnimationManager();

        MKConfig.init();
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        PacketHandler.setupHandler();
        CoreCapabilities.registerCapabilities();
        MKCommand.registerArguments();
        ParticleAnimationManager.setupDeserializers();
        AbilityManager.setupDeserializers();
    }

    private void loadComplete(final FMLLoadCompleteEvent event) {
        // Hopefully other mods will have put their entries in the GlobalEntityTypeAttributes by now
        event.enqueueWork(this::registerAttributes);
    }

    public void modifyAttributesEvent(EntityAttributeModificationEvent event) {
        event.getTypes().forEach(entityType -> {
            if (entityType == EntityType.PLAYER) {
                MKAttributes.iteratePlayerAttributes(attr -> event.add(entityType, attr));
            }
            MKAttributes.iterateEntityAttributes((attr) -> event.add(entityType, attr));
        });
    }

    private void registerAttributes() {
        Attributes.ATTACK_DAMAGE.setShouldWatch(true);
    }

    @SubscribeEvent
    public void serverStart(FMLServerAboutToStartEvent event) {
        // some preinit code
//        LOGGER.info("HELLO FROM ABOUTTOSTART");
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new MKOverlay());
        ClientEventHandler.initKeybindings();
        PlayerPageRegistry.init();
        MKRenderers.registerPlayerRenderers();
        CoreItems.registerItemProperties();
        ClientEventHandler.setupAttributeRenderers();
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
//        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        MKCommand.registerCommands(event.getDispatcher());
    }

    @SubscribeEvent
    public void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(abilityManager);
        event.addListener(talentManager);
        event.addListener(particleAnimationManager);
    }

    private void processIMC(final InterModProcessEvent event) {
        MKCore.LOGGER.debug("MKCore.processIMC");
        internalIMCStageSetup();
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> PlayerPageRegistry::checkClientIMC);
        event.getIMCStream().forEach(m -> {
            if (m.getMethod().equals(PERSONA_EXTENSION)) {
                MKCore.LOGGER.debug("IMC register persona extension from mod {} {}", m.getSenderModId(), m.getMethod());
                IPersonaExtensionProvider factory = (IPersonaExtensionProvider) m.getMessageSupplier().get();
                PersonaManager.registerExtension(factory);
            } else if (m.getMethod().equals(CORE_EXTENSION)){
                MKCore.LOGGER.debug("IMC core extension from mod {} {}", m.getSenderModId(), m.getMethod());
                ICoreExtension extension = (ICoreExtension) m.getMessageSupplier().get();
                extension.register();
            }
        });
    }

    private void internalIMCStageSetup(){
        CoreParticles.HandleEditorParticleRegistration();
    }

    public static ResourceLocation makeRL(String path) {
        return new ResourceLocation(MKCore.MOD_ID, path);
    }

    public static LazyOptional<MKPlayerData> getPlayer(Entity playerEntity) {
        return playerEntity.getCapability(CoreCapabilities.PLAYER_CAPABILITY);
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    public static MKPlayerData getPlayerOrNull(Entity playerEntity) {
        return playerEntity.getCapability(CoreCapabilities.PLAYER_CAPABILITY).orElse(null);
    }

    public static LazyOptional<? extends IMKEntityData> getEntityData(@Nullable Entity entity) {
        if (entity instanceof PlayerEntity) {
            return entity.getCapability(CoreCapabilities.PLAYER_CAPABILITY);
        } else if (entity instanceof LivingEntity) {
            return entity.getCapability(CoreCapabilities.ENTITY_CAPABILITY);
        }
        return LazyOptional.empty();
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    public static IMKEntityData getEntityDataOrNull(@Nullable Entity entity) {
        return getEntityData(entity).orElse(null);
    }

    public static TalentManager getTalentManager() {
        return INSTANCE.talentManager;
    }

    public static AbilityManager getAbilityManager() {
        return INSTANCE.abilityManager;
    }

    public static ParticleAnimationManager getAnimationManager() { return INSTANCE.particleAnimationManager; }
}
