package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.entities.MKAreaEffectEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CoreEntities {

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> evt) {
        evt.getRegistry().register(EntityType.Builder.<MKAreaEffectEntity>create(MKAreaEffectEntity::new, EntityClassification.MISC)
                .immuneToFire()
                .size(0, 0)
                .setTrackingRange(10)
                .setUpdateInterval(10)
                .setShouldReceiveVelocityUpdates(true)
                .disableSummoning()
                .disableSerialization()
                .build("mk_area_effect")
                .setRegistryName(MKCore.makeRL("mk_area_effect")));
    }
}
