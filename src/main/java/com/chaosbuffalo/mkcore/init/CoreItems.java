package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKAbilityProvider;
import com.chaosbuffalo.mkcore.test.abilities.BurningSoul;
import com.chaosbuffalo.mkcore.test.abilities.WhirlwindBlades;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(MKCore.MOD_ID)
public class CoreItems {

    @ObjectHolder("ability_chest")
    public static Item test_armor;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new AbilityArmor(ArmorMaterial.IRON, EquipmentSlotType.CHEST, new Item.Properties())
                .setRegistryName(MKCore.makeRL("ability_chest")));
        event.getRegistry().register(new AbilitySword().setRegistryName(MKCore.makeRL("ability_sword")));
    }

    public static class AbilityArmor extends ArmorItem implements IMKAbilityProvider {

        public AbilityArmor(IArmorMaterial materialIn, EquipmentSlotType slot, Properties builder) {
            super(materialIn, slot, builder);
        }

        @Override
        public MKAbility getAbility(ItemStack item) {
            return BurningSoul.INSTANCE;
        }
    }

    public static class AbilitySword extends SwordItem implements IMKAbilityProvider {

        public AbilitySword() {
            super(ItemTier.IRON, 3, -2.4F, (new Item.Properties()).group(ItemGroup.COMBAT));
        }

        @Override
        public MKAbility getAbility(ItemStack item) {
            return WhirlwindBlades.INSTANCE;
        }
    }
}
