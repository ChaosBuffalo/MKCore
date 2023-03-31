package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mkcore.core.talents.MKTalent;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKCoreRegistry {
    public static final ResourceLocation INVALID_ABILITY = new ResourceLocation(MKCore.MOD_ID, "ability.invalid");
    public static final ResourceLocation INVALID_TALENT = new ResourceLocation(MKCore.MOD_ID, "talent.invalid");
    public static final ResourceLocation INVALID_ENTITLEMENT = new ResourceLocation(MKCore.MOD_ID, "entitlement.invalid");
    public static final ResourceLocation ABILITY_REGISTRY_NAME = MKCore.makeRL("abilities");
    public static IForgeRegistry<MKAbility> ABILITIES = null;
    public static IForgeRegistry<MKDamageType> DAMAGE_TYPES = null;
    public static IForgeRegistry<MKEffect> EFFECTS = null;
    public static IForgeRegistry<MKTalent> TALENTS = null;
    public static IForgeRegistry<MKEntitlement> ENTITLEMENTS = null;

    @Nullable
    public static MKAbility getAbility(ResourceLocation abilityId) {
        return ABILITIES.getValue(abilityId);
    }

    @Nullable
    public static MKDamageType getDamageType(ResourceLocation damageTypeId) {
        return DAMAGE_TYPES.getValue(damageTypeId);
    }

    @Nullable
    public static MKEntitlement getEntitlement(ResourceLocation entitlementId) {
        return ENTITLEMENTS.getValue(entitlementId);
    }

    @SubscribeEvent
    public static void createRegistries(NewRegistryEvent event) {
        event.create(new RegistryBuilder<MKAbility>()
                .setName(ABILITY_REGISTRY_NAME)
                .setType(MKAbility.class), r -> ABILITIES = r);
        event.create(new RegistryBuilder<MKDamageType>()
                .setName(MKCore.makeRL("damage_types"))
                .setType(MKDamageType.class), r -> DAMAGE_TYPES = r);
        event.create(new RegistryBuilder<MKEffect>()
                .setName(MKCore.makeRL("effects"))
                .setType(MKEffect.class), r -> EFFECTS = r);
        event.create(new RegistryBuilder<MKTalent>()
                .setName(MKCore.makeRL("talents"))
                .setType(MKTalent.class), r -> TALENTS = r);
        event.create(new RegistryBuilder<MKEntitlement>()
                .setName(MKCore.makeRL("entitlements"))
                .setType(MKEntitlement.class), r -> ENTITLEMENTS = r);
    }
}
