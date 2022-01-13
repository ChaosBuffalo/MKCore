package com.chaosbuffalo.mkcore.abilities.training.requirements;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingRequirement;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;

public class HasEntitlementRequirement extends AbilityTrainingRequirement {
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKCore.MOD_ID, "training_req.has_entitlement");
    private MKEntitlement entitlement;

    public HasEntitlementRequirement(MKEntitlement entitlement) {
        super(TYPE_NAME);
        this.entitlement = entitlement;
    }

    public <D> HasEntitlementRequirement(Dynamic<D> dynamic){
        super(TYPE_NAME, dynamic);
    }

    @Override
    public boolean check(MKPlayerData playerData, MKAbility ability) {
        return playerData.getEntitlements().hasEntitlement(entitlement);
    }

    @Override
    public void onLearned(MKPlayerData playerData, MKAbility ability) {

    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("entitlement"), ops.createString(entitlement.getRegistryName().toString()));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        ResourceLocation entitlementId = dynamic.get("entitlement").asString()
                .resultOrPartial(MKCore.LOGGER::error)
                .map(ResourceLocation::new)
                .orElse(INVALID_OPTION);
        entitlement = MKCoreRegistry.getEntitlement(entitlementId);
    }

    @Override
    public IFormattableTextComponent describe(MKPlayerData playerData) {
        return new StringTextComponent("You must have earned: ")
                .appendSibling(entitlement.getDescription());
    }

}
