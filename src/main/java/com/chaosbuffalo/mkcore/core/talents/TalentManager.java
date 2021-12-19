package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.PassiveTalentAbility;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.TalentDefinitionSyncPacket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TalentManager extends JsonReloadListener {
    public static String DEFINITION_FOLDER = "player_talents";

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    private final Map<ResourceLocation, TalentTreeDefinition> talentTreeMap = new HashMap<>();
    private boolean serverStarted = false;
    private Collection<TalentTreeDefinition> defaultTrees;

    public TalentManager() {
        super(GSON, DEFINITION_FOLDER);
        this.defaultTrees = null;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void serverStart(FMLServerAboutToStartEvent event) {
        // Can't start sending packets before this event
        serverStarted = true;
    }

    @SubscribeEvent
    public void serverStop(FMLServerStoppingEvent event) {
        serverStarted = false;
    }

    @Override
    protected void apply(@Nonnull Map<ResourceLocation, JsonElement> objectIn,
                         @Nonnull IResourceManager resourceManagerIn,
                         @Nonnull IProfiler profilerIn) {

        MKCore.LOGGER.info("Loading Talent definitions from json");
        boolean wasChanged = false;
        for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
            ResourceLocation location = entry.getKey();
            if (location.getPath().startsWith("_"))
                continue; //Forge: filter anything beginning with "_" as it's used for metadata.
            if (parse(entry.getKey(), entry.getValue().getAsJsonObject())) {
                wasChanged = true;
            }
        }
        if (serverStarted && wasChanged) {
            defaultTrees = null;
            syncToPlayers();
        }

    }

    private boolean parse(ResourceLocation loc, JsonObject json) {
        MKCore.LOGGER.debug("Parsing Talent Tree Json for {}", loc);
        ResourceLocation treeId = new ResourceLocation(loc.getNamespace(), "talent_tree." + loc.getPath());

        TalentTreeDefinition talentTree = TalentTreeDefinition.deserialize(treeId, new Dynamic<>(JsonOps.INSTANCE, json));

        registerTalentTree(talentTree);
        return true;
    }

    public TalentTreeDefinition getTalentTree(ResourceLocation treeId) {
        return talentTreeMap.get(treeId);
    }

    public Collection<TalentTreeDefinition> getDefaultTrees(){
        if (defaultTrees == null){
            defaultTrees = talentTreeMap.values().stream().filter(TalentTreeDefinition::isDefault)
                    .collect(Collectors.toList());
        }
        return defaultTrees;
    }

    public void registerTalentTree(TalentTreeDefinition tree) {
        talentTreeMap.put(tree.getTreeId(), tree);
    }

    public Collection<ResourceLocation> getTreeNames() {
        return Collections.unmodifiableCollection(talentTreeMap.keySet());
    }

    public static PassiveTalentAbility getPassiveTalentAbility(ResourceLocation abilityId) {
        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability instanceof PassiveTalentAbility) {
            return (PassiveTalentAbility) ability;
        }
        return null;
    }

    public static MKAbility getTalentAbility(ResourceLocation talentId) {
        MKTalent talent = MKCoreRegistry.TALENTS.getValue(talentId);
        if (talent instanceof IAbilityTalent<?>) {
            return ((IAbilityTalent<?>) talent).getAbility();
        } else {
            return null;
        }
    }

    public void syncToPlayers() {
        TalentDefinitionSyncPacket updatePacket = new TalentDefinitionSyncPacket(talentTreeMap.values());
        PacketHandler.sendToAll(updatePacket);
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void playerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            TalentDefinitionSyncPacket updatePacket = new TalentDefinitionSyncPacket(talentTreeMap.values());
            PacketHandler.sendMessage(updatePacket, (ServerPlayerEntity) event.getPlayer());
        }
    }
}
