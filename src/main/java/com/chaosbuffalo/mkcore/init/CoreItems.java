package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKAbilityProvider;
import com.chaosbuffalo.mkcore.test.MKTestAbilities;
import com.chaosbuffalo.mkcore.test.abilities.EmberAbility;
import com.chaosbuffalo.mkcore.test.abilities.WhirlwindBlades;
import com.chaosbuffalo.mkcore.test.abilities.NewBurningSoul;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(MKCore.MOD_ID)
public class CoreItems {

    @ObjectHolder("ability_chest")
    public static Item test_armor;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(
                new AbilityArmor(ArmorMaterial.IRON, EquipmentSlotType.CHEST, new Item.Properties(), MKTestAbilities.TEST_NEW_BURNING_SOUL)
                        .setRegistryName(MKCore.makeRL("ability_chest")));
        event.getRegistry().register(new AbilitySword().setRegistryName(MKCore.makeRL("ability_sword")));
        event.getRegistry().register(
                new AbilityArmor(ArmorMaterial.IRON, EquipmentSlotType.FEET, new Item.Properties(), MKTestAbilities.TEST_EMBER)
                        .setRegistryName(MKCore.makeRL("ability_boots")));
    }

    public static class AbilityArmor extends ArmorItem implements IMKAbilityProvider {
        private final Supplier<? extends MKAbility> ability;

        public AbilityArmor(IArmorMaterial materialIn, EquipmentSlotType slot, Properties builder, Supplier<? extends MKAbility>  ability) {
            super(materialIn, slot, builder);
            this.ability = ability;
        }

        @Override
        public MKAbility getAbility(ItemStack item) {
            return ability.get();
        }
    }

    public static class AbilitySword extends SwordItem implements IMKAbilityProvider {

        public AbilitySword() {
            super(ItemTier.IRON, 3, -2.4F, (new Item.Properties()).group(ItemGroup.COMBAT));
        }

        @Override
        public MKAbility getAbility(ItemStack item) {
            return MKTestAbilities.TEST_WHIRLWIND_BLADES.get();
        }
    }

    public static void registerItemProperties(){
        List<Item> swordsToAddBlocking = new ArrayList<>();
        swordsToAddBlocking.add(Items.DIAMOND_SWORD);
        swordsToAddBlocking.add(Items.WOODEN_SWORD);
        swordsToAddBlocking.add(Items.STONE_SWORD);
        swordsToAddBlocking.add(Items.IRON_SWORD);
        swordsToAddBlocking.add(Items.GOLDEN_SWORD);
        swordsToAddBlocking.add(Items.NETHERITE_SWORD);
        for (Item sword : swordsToAddBlocking){

            ItemModelsProperties.registerProperty(sword, new ResourceLocation("blocking"),
                    (itemStack, world, entity) -> entity != null && entity.isHandActive()
                            && entity.getActiveItemStack() == itemStack ? 1.0F : 0.0F);
        }


    }
}
