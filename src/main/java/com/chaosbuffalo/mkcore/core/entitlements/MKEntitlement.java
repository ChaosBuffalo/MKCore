package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.core.records.IRecordType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class MKEntitlement extends ForgeRegistryEntry<MKEntitlement> {
    private final int maxEntitlements;

    public MKEntitlement(ResourceLocation name, int maxEntitlements) {
        setRegistryName(name);
        this.maxEntitlements = maxEntitlements;
    }

    public int getMaxEntitlements() {
        return maxEntitlements;
    }

    public abstract IRecordType<?> getRecordType();

    public ITextComponent getDescription(){
        return new TranslationTextComponent(String.format("%s.entitlement.%s.name",
                getRegistryName().getNamespace(), getRegistryName().getPath()));
    }
}
