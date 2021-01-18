package com.chaosbuffalo.mkcore.sync;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;


public class SyncMapUpdater<K, V extends IMKSerializable<CompoundNBT>> implements ISyncObject {

    private final String rootName;
    private final Supplier<Map<K, V>> mapSupplier;
    private final Function<K, String> keyEncoder;
    private final Function<String, K> keyDecoder;
    private final Set<K> dirty = new HashSet<>();
    private final Function<K, V> factory;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public SyncMapUpdater(String rootName, Supplier<Map<K, V>> mapSupplier,
                          Function<K, String> keyEncoder, Function<String, K> keyDecoder, Function<K, V> factory) {
        this.rootName = rootName;
        this.mapSupplier = mapSupplier;
        this.keyEncoder = keyEncoder;
        this.keyDecoder = keyDecoder;
        this.factory = factory;
    }

    public void markDirty(K key) {
        dirty.add(key);
        parentNotifier.notifyUpdate(this);
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
    public void deserializeUpdate(CompoundNBT tag) {
        CompoundNBT root = tag.getCompound(rootName);

        if (root.getBoolean("f")) {
            mapSupplier.get().clear();
        }

        deserializeMap(root.getCompound("l"));
    }

    @Override
    public void serializeUpdate(CompoundNBT tag) {
        if (dirty.isEmpty())
            return;

        CompoundNBT root = new CompoundNBT();
        root.put("l", serializeMap(dirty, this::defaultEntryFilter));
        tag.put(rootName, root);

        dirty.clear();
    }

    @Override
    public void serializeFull(CompoundNBT tag) {
        CompoundNBT root = new CompoundNBT();
        root.putBoolean("f", true);
        root.put("l", serializeStorage());
        tag.put(rootName, root);

        dirty.clear();
    }

    private boolean defaultEntryFilter(K key, V value) {
        return true;
    }

    private CompoundNBT serializeMap(Collection<K> keyCollection, BiPredicate<K, V> entryFilter) {
        CompoundNBT list = new CompoundNBT();
        Map<K, V> map = mapSupplier.get();
        keyCollection.forEach(key -> {
            V value = map.get(key);
            if (value != null && entryFilter.test(key, value)) {
                list.put(keyEncoder.apply(key), value.serialize());
            }
        });
        return list;
    }

    private void deserializeMap(CompoundNBT tag) {
        Map<K, V> map = mapSupplier.get();
        for (String key : tag.keySet()) {
            CompoundNBT entryTag = tag.getCompound(key);
            K decodedKey = keyDecoder.apply(key);
            if (decodedKey == null) {
                MKCore.LOGGER.error("Failed to decode map key {}", key);
                continue;
            }
            V current = map.computeIfAbsent(decodedKey, factory);
            if (current == null) {
                MKCore.LOGGER.error("Failed to compute map value for key {}", decodedKey);
                continue;
            }

            if (!current.deserialize(entryTag)) {
                MKCore.LOGGER.error("Failed to deserialize map value for {}", decodedKey);
                continue;
            }
            map.put(decodedKey, current);
        }
    }

    public INBT serializeStorage() {
        return serializeMap(mapSupplier.get().keySet(), this::defaultEntryFilter);
    }

    public INBT serializeStorage(BiPredicate<K, V> entryFilter) {
        return serializeMap(mapSupplier.get().keySet(), entryFilter);
    }

    public void deserializeStorage(INBT tag) {
        if (tag instanceof CompoundNBT) {
            CompoundNBT list = (CompoundNBT) tag;
            mapSupplier.get().clear();
            deserializeMap(list);
        }
    }
}
