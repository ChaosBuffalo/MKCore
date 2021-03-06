package com.chaosbuffalo.mkcore.effects;

import net.minecraft.entity.LivingEntity;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public class SpellManager {

    private static final WeakHashMap<LivingEntity, Map<SpellEffectBase, SpellCast>> allCasts = new WeakHashMap<>();

    public static Optional<SpellCast> getCast(LivingEntity target, SpellEffectBase spellEffectBase) {
        Map<SpellEffectBase, SpellCast> targetSpells = allCasts.get(target);
        if (targetSpells == null) {
            return Optional.empty();
        }

        SpellCast cast = targetSpells.get(spellEffectBase);
        if (cast != null) {
            cast.updateRefs(target.world);
        }
        return Optional.ofNullable(cast);
    }

    public static void registerTarget(SpellCast cast, LivingEntity target) {
        allCasts.computeIfAbsent(target, k -> new IdentityHashMap<>()).put(cast.getPotion(), cast);
    }
}
