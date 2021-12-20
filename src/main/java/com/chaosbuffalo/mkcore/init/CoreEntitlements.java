package com.chaosbuffalo.mkcore.init;


import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mkcore.core.player.AbilityPoolEntitlement;
import com.chaosbuffalo.mkcore.core.player.AbilitySlotEntitlement;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(MKCore.MOD_ID)
public class CoreEntitlements {

    @ObjectHolder("ability_slot.basic")
    public static MKEntitlement BasicAbilitySlotCount;

    @ObjectHolder("ability_slot.passive")
    public static MKEntitlement PassiveAbilitySlotCount;

    @ObjectHolder("ability_slot.ultimate")
    public static MKEntitlement UltimateAbilitySlotCount;

    @ObjectHolder("ability_pool.count")
    public static MKEntitlement AbilityPoolCount;

    @SubscribeEvent
    public static void registerEntitlements(RegistryEvent.Register<MKEntitlement> evt) {
        evt.getRegistry().register(new AbilitySlotEntitlement(MKCore.makeRL("ability_slot.basic"), AbilityGroupId.Basic));
        evt.getRegistry().register(new AbilitySlotEntitlement(MKCore.makeRL("ability_slot.passive"), AbilityGroupId.Passive));
        evt.getRegistry().register(new AbilitySlotEntitlement(MKCore.makeRL("ability_slot.ultimate"), AbilityGroupId.Ultimate));
        evt.getRegistry().register(new AbilityPoolEntitlement(MKCore.makeRL("ability_pool.count"), GameConstants.MAX_ABILITY_POOL_SIZE - GameConstants.DEFAULT_ABILITY_POOL_SIZE));
    }
}
