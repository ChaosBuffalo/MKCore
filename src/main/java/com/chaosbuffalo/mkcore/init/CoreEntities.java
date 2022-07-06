package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.entities.LineEffectEntity;
import com.chaosbuffalo.mkcore.entities.MKAreaEffectEntity;
import com.chaosbuffalo.mkcore.entities.PointEffectEntity;
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
                .setTrackingRange(5)
                .setUpdateInterval(10)
                .setShouldReceiveVelocityUpdates(true)
                .disableSummoning()
                .disableSerialization()
                .build("mk_area_effect")
                .setRegistryName(MKCore.makeRL("mk_area_effect")));

        evt.getRegistry().register(EntityType.Builder.<LineEffectEntity>create(LineEffectEntity::new, EntityClassification.MISC)
                .immuneToFire()
                .size(0, 0)
                .setTrackingRange(5)
                .setUpdateInterval(10)
                .setShouldReceiveVelocityUpdates(true)
                .disableSummoning()
                .disableSerialization()
                .build("mk_line_effect")
                .setRegistryName(MKCore.makeRL("mk_line_effect")));

        evt.getRegistry().register(EntityType.Builder.<PointEffectEntity>create(PointEffectEntity::new, EntityClassification.MISC)
                .immuneToFire()
                .size(1, 1)
                .setTrackingRange(5)
                .setUpdateInterval(10)
                .setShouldReceiveVelocityUpdates(true)
                .disableSummoning()
                .disableSerialization()
                .build("mk_point_effect")
                .setRegistryName(MKCore.makeRL("mk_point_effect")));
    }
}
