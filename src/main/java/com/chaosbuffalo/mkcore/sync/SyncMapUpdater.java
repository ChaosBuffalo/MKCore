package com.chaosbuffalo.mkcore.sync;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraftforge.common.util.Constants;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;


public class SyncMapUpdater<K, V extends IMKSerializable<CompoundNBT>> implements ISyncObject {

    private final String rootName;
    private final Map<K, V> backingMap;
    private final Function<K, String> keyEncoder;
    private final Function<String, K> keyDecoder;
    private final Set<K> dirty = new HashSet<>();
    private final Function<K, V> valueFactory;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;
    private BiPredicate<K, V> storageFilter = this::defaultEntryFilter;
    private BiPredicate<K, V> syncFilter = this::defaultEntryFilter;

    public SyncMapUpdater(String rootName,
                          Supplier<Map<K, V>> mapSupplier,
                          Function<K, String> keyEncoder,
                          Function<String, K> keyDecoder,
                          Function<K, V> valueFactory) {
        this.rootName = rootName;
        this.backingMap = mapSupplier.get();
        this.keyEncoder = keyEncoder;
        this.keyDecoder = keyDecoder;
        this.valueFactory = valueFactory;
    }

    public void setStorageFilter(BiPredicate<K, V> filter) {
        storageFilter = filter != null ? filter : this::defaultEntryFilter;
    }

    public void setSyncFilter(BiPredicate<K, V> filter) {
        syncFilter = filter != null ? filter : this::defaultEntryFilter;
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

    private ListNBT gatherDirtyRemovals() {
        if (dirty.isEmpty())
            return null;
        ListNBT list = new ListNBT();

        dirty.removeIf(key -> {
            V value = backingMap.get(key);
            if (value == null) {
                list.add(StringNBT.valueOf(keyEncoder.apply(key)));
                return true;
            }
            return false;
        });

        return list;
    }

    private void processDirtyRemovals(ListNBT list) {
        for (int i = 0; i < list.size(); i++) {
            String encodedKey = list.getString(i);
            if (encodedKey.isEmpty())
                continue;

            K key = keyDecoder.apply(encodedKey);
            V old = backingMap.remove(key);
            MKCore.LOGGER.info("removing {} {} {} {}", encodedKey, key, old != null, backingMap.size());
        }
    }

    @Override
    public void deserializeUpdate(CompoundNBT tag) {
        CompoundNBT root = tag.getCompound(rootName);

        if (root.getBoolean("f")) {
            backingMap.clear();
        }

        if (root.contains("r")) {
            // server has deleted entries, so remove them from the local map
            processDirtyRemovals(root.getList("r", Constants.NBT.TAG_STRING));
        }

        if (root.contains("l")) {
            deserializeMap(root.getCompound("l"), IMKSerializable::deserializeSync);
        }
    }

    private CompoundNBT makeSyncMap(Collection<K> keySet) {
        return serializeMap(keySet, IMKSerializable::serializeSync, syncFilter);
    }

    @Override
    public void serializeUpdate(CompoundNBT tag) {
        if (dirty.isEmpty())
            return;

        CompoundNBT root = new CompoundNBT();
        ListNBT removals = gatherDirtyRemovals();
        if (removals != null && !removals.isEmpty()) {
            root.put("r", removals);
        }

        CompoundNBT updates = makeSyncMap(dirty);
        if (!updates.isEmpty()) {
            root.put("l", updates);
        }
        tag.put(rootName, root);

        dirty.clear();
    }

    @Override
    public void serializeFull(CompoundNBT tag) {
        CompoundNBT root = new CompoundNBT();
        root.putBoolean("f", true);
        root.put("l", makeSyncMap(backingMap.keySet()));
        tag.put(rootName, root);

        dirty.clear();
    }

    private boolean defaultEntryFilter(K key, V value) {
        return true;
    }

    private CompoundNBT serializeMap(Collection<K> keyCollection,
                                     Function<V, INBT> valueSerializer,
                                     BiPredicate<K, V> entryFilter) {
        CompoundNBT list = new CompoundNBT();
        keyCollection.forEach(key -> {
            V value = backingMap.get(key);
            if (value == null)
                return;

            if (!entryFilter.test(key, value))
                return;

            INBT tag = valueSerializer.apply(value);
            if (tag != null) {
                list.put(keyEncoder.apply(key), tag);
            }
        });
        return list;
    }

    private void deserializeMap(CompoundNBT tag,
                                BiPredicate<V, CompoundNBT> valueDeserializer) {
        for (String key : tag.keySet()) {
            K decodedKey = keyDecoder.apply(key);
            if (decodedKey == null) {
                MKCore.LOGGER.error("Failed to decode map key {}", key);
                continue;
            }

            V current = backingMap.computeIfAbsent(decodedKey, valueFactory);
            if (current == null) {
                MKCore.LOGGER.error("Failed to compute map value for key {}", decodedKey);
                continue;
            }

            CompoundNBT entryTag = tag.getCompound(key);
            if (!valueDeserializer.test(current, entryTag)) {
                MKCore.LOGGER.error("Failed to deserialize map value for {}", decodedKey);
                continue;
            }
            backingMap.put(decodedKey, current);
        }
    }

    public CompoundNBT serializeStorage() {
        return serializeStorage(storageFilter);
    }

    public CompoundNBT serializeStorage(BiPredicate<K, V> entryFilter) {
        return serializeMap(backingMap.keySet(), IMKSerializable::serializeStorage, entryFilter);
    }

    public void deserializeStorage(INBT tag) {
        if (tag instanceof CompoundNBT) {
            backingMap.clear();
            deserializeMap((CompoundNBT) tag, IMKSerializable::deserializeStorage);
        }
    }
}
