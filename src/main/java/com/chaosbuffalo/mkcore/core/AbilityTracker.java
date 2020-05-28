package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.sync.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.util.*;
import java.util.function.BiConsumer;

public class AbilityTracker implements ISyncObject {

    private int ticks;
    private final Map<ResourceLocation, Cooldown> cooldowns = new HashMap<>();

    public boolean hasCooldown(ResourceLocation id) {
        return getCooldownTicks(id) > 0;
    }

    public float getCooldown(ResourceLocation id, float partialTicks) {
        Cooldown cd = this.cooldowns.get(id);

        if (cd != null) {
            float totalCooldown = (float) (cd.expireTicks - cd.createTicks);
            float currentCooldown = (float) cd.expireTicks - ((float) this.ticks + partialTicks);
            return MathHelper.clamp(currentCooldown / totalCooldown, 0.0F, 1.0F);
        } else {
            return 0.0F;
        }
    }

    public int getCooldownTicks(ResourceLocation id) {
        Cooldown cd = this.cooldowns.get(id);

        if (cd != null) {
            return Math.max(0, cd.expireTicks - this.ticks);
        } else {
            return 0;
        }
    }

    public int getMaxCooldownTicks(ResourceLocation id) {
        Cooldown cd = this.cooldowns.get(id);

        if (cd != null) {
            return Math.max(0, cd.expireTicks - cd.createTicks);
        } else {
            return 0;
        }
    }

    public void tick() {
        ticks++;

        if (!this.cooldowns.isEmpty()) {
            Iterator<Map.Entry<ResourceLocation, Cooldown>> iterator = this.cooldowns.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<ResourceLocation, Cooldown> entry = iterator.next();

                if (entry.getValue().expireTicks <= this.ticks) {
                    iterator.remove();
                    this.notifyOnRemove(entry.getKey());
                }
            }
        }
    }

    public void setCooldown(ResourceLocation id, int ticksIn) {
        setCooldownInternal(id, ticksIn);
        this.notifyOnSet(id, ticksIn);
    }

    protected void setCooldownInternal(ResourceLocation id, int ticksIn) {
        this.cooldowns.put(id, new Cooldown(this.ticks, this.ticks + ticksIn));
    }

    public void removeCooldown(ResourceLocation id) {
        this.cooldowns.remove(id);
        this.notifyOnRemove(id);
    }

    protected void notifyOnSet(ResourceLocation id, int ticksIn) {
    }

    protected void notifyOnRemove(ResourceLocation id) {
    }

    protected void sync() {
    }

    public void serialize(CompoundNBT nbt) {
        CompoundNBT root = new CompoundNBT();
        iterateActive((id, cd) -> root.putInt(id.toString(), cd));
        nbt.put("cooldowns", root);
    }

    public void deserialize(CompoundNBT nbt) {
        if (nbt.contains("cooldowns")) {
            CompoundNBT root = nbt.getCompound("cooldowns");
            for (String key : root.keySet()) {
                setCooldownInternal(new ResourceLocation(key), root.getInt(key));
            }
        }
    }

    void iterateActive(BiConsumer<ResourceLocation, Integer> consumer) {
        for (ResourceLocation id : cooldowns.keySet()) {
            int cd = getCooldownTicks(id);
            if (cd > 0) {
                consumer.accept(id, cd);
            }
        }
    }

    void removeAll() {
        Iterator<Map.Entry<ResourceLocation, Cooldown>> iterator = this.cooldowns.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ResourceLocation, Cooldown> entry = iterator.next();
            iterator.remove();
            this.notifyOnRemove(entry.getKey());
        }
    }

    static class Cooldown {
        final int createTicks;
        final int expireTicks;

        private Cooldown(int createTicksIn, int expireTicksIn) {
            this.createTicks = createTicksIn;
            this.expireTicks = expireTicksIn;
        }
    }

    static class AbilityTrackerServer extends AbilityTracker {

        private final ServerPlayerEntity player;
        private final List<ResourceLocation> dirty = new ArrayList<>();
        private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

        public AbilityTrackerServer(ServerPlayerEntity player) {
            this.player = player;
        }

        @Override
        protected void notifyOnSet(ResourceLocation id, int ticksIn) {
            super.notifyOnSet(id, ticksIn);
            dirty.add(id);
            parentNotifier.markDirty(this);
        }

        @Override
        protected void notifyOnRemove(ResourceLocation id) {
            super.notifyOnRemove(id);
            dirty.add(id);
            parentNotifier.markDirty(this);
        }

        @Override
        public void setNotifier(ISyncNotifier notifier) {
            parentNotifier = notifier;
        }

        @Override
        public boolean isDirty() {
            return dirty.size() > 0;
        }

        @Override
        public void serializeUpdate(CompoundNBT tag) {
            CompoundNBT root = new CompoundNBT();
            dirty.forEach(id -> root.putInt(id.toString(), getCooldownTicks(id)));
            tag.put("cooldowns", root);
            dirty.clear();
        }

        @Override
        public void serializeFull(CompoundNBT tag) {
            serialize(tag);
        }

        @Override
        protected void sync() {
            iterateActive(this::notifyOnSet);
        }
    }

    public static AbilityTracker getTracker(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            return new AbilityTrackerServer((ServerPlayerEntity) player);
        } else {
            return new AbilityTracker();
        }
    }

    @Override
    public void setNotifier(ISyncNotifier notifier) {

    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public void deserializeUpdate(CompoundNBT tag) {
        deserialize(tag);
    }

    @Override
    public void serializeUpdate(CompoundNBT tag) {

    }

    @Override
    public void serializeFull(CompoundNBT tag) {

    }
}
