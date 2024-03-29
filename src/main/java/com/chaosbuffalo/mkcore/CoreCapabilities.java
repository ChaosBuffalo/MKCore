package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class CoreCapabilities {

    public static final ResourceLocation PLAYER_CAP_ID = MKCore.makeRL("player_data");
    public static final ResourceLocation ENTITY_CAP_ID = MKCore.makeRL("entity_data");
    private static final List<Predicate<LivingEntity>> entityAdditionPredicates = new ArrayList<>();

    @CapabilityInject(MKPlayerData.class)
    public static final Capability<MKPlayerData> PLAYER_CAPABILITY;

    @CapabilityInject(MKEntityData.class)
    public static final Capability<MKEntityData> ENTITY_CAPABILITY;

    static {
        PLAYER_CAPABILITY = null;
        ENTITY_CAPABILITY = null;
    }

    public static void registerCapabilities() {
        CapabilityManager.INSTANCE.register(MKPlayerData.class, new MKDataStorage<>(), () -> null);
        CapabilityManager.INSTANCE.register(MKEntityData.class, new MKDataStorage<>(), () -> null);
        MinecraftForge.EVENT_BUS.register(CoreCapabilities.class);
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity) e.getObject();
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

    public static class MKDataStorage<T extends IMKEntityData> implements Capability.IStorage<T> {

        @Nullable
        @Override
        public INBT writeNBT(Capability<T> capability, T instance, Direction side) {
            return instance.serialize();
        }

        @Override
        public void readNBT(Capability<T> capability, T instance, Direction side, INBT nbt) {
            if (nbt instanceof CompoundNBT && instance != null) {
                CompoundNBT tag = (CompoundNBT) nbt;
                instance.deserialize(tag);
            }
        }
    }

    public static class EntityDataProvider implements ICapabilitySerializable<CompoundNBT> {
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
        public CompoundNBT serializeNBT() {
            return entityHandler.serialize();
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            entityHandler.deserialize(nbt);
        }
    }


    public static class PlayerDataProvider implements ICapabilitySerializable<CompoundNBT> {
        private final MKPlayerData playerHandler;
        private final LazyOptional<MKPlayerData> capOpt;

        public PlayerDataProvider(PlayerEntity playerEntity) {
            playerHandler = new MKPlayerData(playerEntity);
            capOpt = LazyOptional.of(() -> playerHandler);
        }

        public static void attach(AttachCapabilitiesEvent<Entity> event, PlayerEntity playerEntity) {
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
        public CompoundNBT serializeNBT() {
            return playerHandler.serialize();
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            playerHandler.deserialize(nbt);
        }
    }
}
