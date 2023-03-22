package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.core.MKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CoreCapabilities {

    public static final ResourceLocation PLAYER_CAP_ID = MKCore.makeRL("player_data");
    public static final ResourceLocation ENTITY_CAP_ID = MKCore.makeRL("entity_data");
    private static final List<Predicate<LivingEntity>> entityAdditionPredicates = new ArrayList<>();

    public static final Capability<MKPlayerData> PLAYER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<MKEntityData> ENTITY_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});


    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(MKPlayerData.class);
        event.register(MKEntityData.class);
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof Player) {
            Player playerEntity = (Player) e.getObject();
            PlayerDataProvider.attach(e, playerEntity);
        } else if (e.getObject() instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) e.getObject();
            EntityDataProvider.attach(e, livingEntity);
        }
    }

    /**
     * @param entityPredicate Predicate to determine if the given entity should be given the IMKEntityData capability
     */
    public static void registerLivingEntity(Predicate<LivingEntity> entityPredicate) {
        entityAdditionPredicates.add(entityPredicate);
    }

    public static class EntityDataProvider implements ICapabilitySerializable<CompoundTag> {
        private final MKEntityData entityHandler;
        private final LazyOptional<MKEntityData> capOpt;

        public EntityDataProvider(LivingEntity entity) {
            entityHandler = new MKEntityData(entity);
            capOpt = LazyOptional.of(() -> entityHandler);
        }

        public static void attach(AttachCapabilitiesEvent<Entity> event, LivingEntity playerEntity) {
            EntityDataProvider provider = new EntityDataProvider(playerEntity);
            event.addCapability(ENTITY_CAP_ID, provider);
            event.addListener(provider.capOpt::invalidate);
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return CoreCapabilities.ENTITY_CAPABILITY.orEmpty(cap, capOpt);
        }

        @Override
        public CompoundTag serializeNBT() {
            return entityHandler.serialize();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            entityHandler.deserialize(nbt);
        }
    }


    public static class PlayerDataProvider implements ICapabilitySerializable<CompoundTag> {
        private final MKPlayerData playerHandler;
        private final LazyOptional<MKPlayerData> capOpt;

        public PlayerDataProvider(Player playerEntity) {
            playerHandler = new MKPlayerData(playerEntity);
            capOpt = LazyOptional.of(() -> playerHandler);
        }

        public static void attach(AttachCapabilitiesEvent<Entity> event, Player playerEntity) {
            PlayerDataProvider provider = new PlayerDataProvider(playerEntity);
            event.addCapability(PLAYER_CAP_ID, provider);
            event.addListener(provider.capOpt::invalidate);
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return CoreCapabilities.PLAYER_CAPABILITY.orEmpty(cap, capOpt);
        }

        @Override
        public CompoundTag serializeNBT() {
            return playerHandler.serialize();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            playerHandler.deserialize(nbt);
        }
    }
}
