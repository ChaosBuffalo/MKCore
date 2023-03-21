package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.entities.LineEffectEntity;
import com.chaosbuffalo.mkcore.entities.MKAreaEffectEntity;
import com.chaosbuffalo.mkcore.entities.PointEffectEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CoreEntities {

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> evt) {
        evt.getRegistry().register(EntityType.Builder.<MKAreaEffectEntity>of(MKAreaEffectEntity::new, MobCategory.MISC)
                .fireImmune()
                .sized(0, 0)
                .setTrackingRange(5)
                .setUpdateInterval(10)
                .setShouldReceiveVelocityUpdates(true)
                .noSummon()
                .noSave()
                .build("mk_area_effect")
                .setRegistryName(MKCore.makeRL("mk_area_effect")));

        evt.getRegistry().register(EntityType.Builder.<LineEffectEntity>of(LineEffectEntity::new, MobCategory.MISC)
                .fireImmune()
                .sized(0, 0)
                .setTrackingRange(5)
                .setUpdateInterval(10)
                .setShouldReceiveVelocityUpdates(true)
                .noSummon()
                .noSave()
                .build("mk_line_effect")
                .setRegistryName(MKCore.makeRL("mk_line_effect")));

        evt.getRegistry().register(EntityType.Builder.<PointEffectEntity>of(PointEffectEntity::new, MobCategory.MISC)
                .fireImmune()
                .sized(1, 1)
                .setTrackingRange(5)
                .setUpdateInterval(10)
                .setShouldReceiveVelocityUpdates(true)
                .noSummon()
                .noSave()
                .build("mk_point_effect")
                .setRegistryName(MKCore.makeRL("mk_point_effect")));
    }
}
