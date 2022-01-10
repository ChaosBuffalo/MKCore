package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingRequirement;
import com.chaosbuffalo.mkcore.abilities.training.requirements.ExperienceLevelRequirement;
import com.chaosbuffalo.mkcore.abilities.training.requirements.HasEntitlementRequirement;
import com.chaosbuffalo.mkcore.abilities.training.requirements.HeldItemRequirement;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerAbilitiesSyncPacket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class AbilityManager extends JsonReloadListener {
    public static final String DEFINITION_FOLDER = "player_abilities";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final Map<ResourceLocation, Function<Dynamic<?>, AbilityTrainingRequirement>> REQ_DESERIALIZERS = new HashMap<>();

    public AbilityManager() {
        super(GSON, DEFINITION_FOLDER);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn,
                         @Nonnull IResourceManager resourceManagerIn,
                         @Nonnull IProfiler profilerIn) {
        MKCore.LOGGER.debug("Loading ability definitions from Json");
        for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            MKCore.LOGGER.debug("Found file: {}", resourcelocation);
            if (resourcelocation.getPath().startsWith("_"))
                continue; //Forge: filter anything beginning with "_" as it's used for metadata.
            parse(entry.getKey(), entry.getValue().getAsJsonObject());
        }
    }

    public static void putAbilityTrainingReqDeserializer(ResourceLocation name, Function<Dynamic<?>, AbilityTrainingRequirement> supplier){
        REQ_DESERIALIZERS.put(name, supplier);
    }

    @Nullable
    public static Function<Dynamic<?>, AbilityTrainingRequirement> getAbilityTrainingReqDeserializer(ResourceLocation name){
        return REQ_DESERIALIZERS.get(name);
    }

    @SubscribeEvent
    public void onDataPackSync(OnDatapackSyncEvent event) {
        MKCore.LOGGER.debug("AbilityManager.onDataPackSync");
        PlayerAbilitiesSyncPacket updatePacket = new PlayerAbilitiesSyncPacket(MKCoreRegistry.ABILITIES.getValues());
        if (event.getPlayer() != null) {
            // sync to single player
            MKCore.LOGGER.debug("Sending {} ability definition update packet", event.getPlayer());
            PacketHandler.sendMessage(updatePacket, event.getPlayer());
        } else {
            // sync to playerlist
            PacketHandler.sendToAll(updatePacket);
        }
    }

    public static void setupDeserializers(){
        putAbilityTrainingReqDeserializer(ExperienceLevelRequirement.TYPE_NAME, ExperienceLevelRequirement::new);
        putAbilityTrainingReqDeserializer(HasEntitlementRequirement.TYPE_NAME, HasEntitlementRequirement::new);
        putAbilityTrainingReqDeserializer(HeldItemRequirement.TYPE_NAME, HeldItemRequirement::new);
    }

    private boolean parse(ResourceLocation loc, JsonObject json) {
        MKCore.LOGGER.debug("Parsing Ability Json for {}", loc);
        ResourceLocation abilityLoc = new ResourceLocation(loc.getNamespace(),
                "ability." + loc.getPath());
        MKAbility ability = MKCoreRegistry.getAbility(abilityLoc);
        if (ability == null) {
            MKCore.LOGGER.warn("Failed to parse ability data for : {}", abilityLoc);
            return false;
        }
        ability.deserializeDynamic(new Dynamic<>(JsonOps.INSTANCE, json));
        return true;
    }
}
