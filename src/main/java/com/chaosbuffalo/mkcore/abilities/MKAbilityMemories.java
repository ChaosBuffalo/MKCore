package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.utils.TargetUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKAbilityMemories {

    @ObjectHolder("mkcore:ability_target")
    public static MemoryModuleType<LivingEntity> ABILITY_TARGET;

    @ObjectHolder("mkcore:ability_position_target")
    public static MemoryModuleType<TargetUtil.LivingOrPosition> ABILITY_POSITION_TARGET;

    @SubscribeEvent
    public static void registerModuleTypes(RegistryEvent.Register<MemoryModuleType<?>> evt) {
        evt.getRegistry().register(new MemoryModuleType<LivingEntity>(Optional.empty())
                .setRegistryName(MKCore.MOD_ID, "ability_target"));
        evt.getRegistry().register(new MemoryModuleType<Vector3d>(Optional.empty())
                .setRegistryName(MKCore.MOD_ID, "ability_position_target"));
    }
}

