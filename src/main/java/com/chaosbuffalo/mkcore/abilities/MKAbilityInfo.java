package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;


public class MKAbilityInfo implements IMKSerializable<CompoundNBT> {
    private final MKAbility ability;
    private final Set<AbilitySource> sources = new HashSet<>(2);
    @Nullable
    private AbilitySource highestSource;

    public MKAbilityInfo(MKAbility ability) {
        this.ability = ability;
    }

    @Nonnull
    public MKAbility getAbility() {
        return ability;
    }

    public ResourceLocation getId() {
        return ability.getAbilityId();
    }

    public boolean isCurrentlyKnown() {
        return sources.size() > 0;
    }

    public Set<AbilitySource> getSources() {
        return sources;
    }

    public boolean hasSource(AbilitySource source) {
        return sources.contains(source);
    }

    public void addSource(AbilitySource source) {
        if (sources.add(source)) {
            updateHighestSource();
        }
    }

    public void removeSource(AbilitySource source) {
        if (sources.remove(source)) {
            updateHighestSource();
        }
    }

    private void updateHighestSource() {
        highestSource = sources.stream()
                .max(Comparator.comparingInt(s -> s.getSourceType().getPriority()))
                .orElse(null);
    }

    public boolean usesAbilityPool() {
        return highestSource != null && highestSource.usesAbilityPool();
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();

        ListNBT list = new ListNBT();
        sources.forEach(s -> list.add(s.serialize()));
        tag.put("sources", list);
        return tag;
    }

    @Override
    public CompoundNBT serializeStorage() {
        CompoundNBT tag = new CompoundNBT();

        ListNBT list = new ListNBT();
        sources.forEach(s -> {
            // Only store sources that will not be reapplied on login
            if (s.getSourceType().isPersistent()) {
                list.add(s.serialize());
            }
        });
        tag.put("sources", list);
        return tag;
    }

    @Override
    public boolean deserialize(CompoundNBT tag) {
        sources.clear();
        if (tag.contains("sources")) {
            ListNBT list = tag.getList("sources", Constants.NBT.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                AbilitySource source = AbilitySource.deserialize(list.getString(i));
                if (source == null) {
                    MKCore.LOGGER.error("Failed to decode AbilitySource {}", list.getString(i));
                    return false;
                }
                addSource(source);
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "MKAbilityInfo{" +
                "ability=" + ability +
                ", sources=" + sources.size() +
                ", highestSource=" + highestSource +
                '}';
    }
}
