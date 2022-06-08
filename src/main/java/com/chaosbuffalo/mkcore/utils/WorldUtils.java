package com.chaosbuffalo.mkcore.utils;

import com.chaosbuffalo.mkcore.GameConstants;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class WorldUtils {

    private static final Map<RegistryKey<World>, Double> difficultyBonuses = new HashMap<>();
    private static final Vector3i CENTER = new Vector3i(0, 0, 0);
    private static final int DIFFICULTY_BAND_SIZE = 500;
    private static final double DIFFICULTY_SCORE_PER_BAND = 10.0;

    static {
        difficultyBonuses.put(World.OVERWORLD, 0.0);
        difficultyBonuses.put(World.THE_NETHER, 25.0);
        difficultyBonuses.put(World.THE_END, 40.0);
    }

    public static void putDifficultyBonus(RegistryKey<World> worldKey, double value) {
        difficultyBonuses.put(worldKey, value);
    }

    public static double getDifficultyForGlobalPos(GlobalPos pos) {
        double diffOffset = difficultyBonuses.getOrDefault(pos.getDimension(), 0.0);
        int manhattenDist = pos.getPos().manhattanDistance(CENTER);
        int divisions = manhattenDist / DIFFICULTY_BAND_SIZE;
        return Math.min(Math.max(GameConstants.MIN_DIFFICULTY, (divisions * DIFFICULTY_SCORE_PER_BAND) + diffOffset), GameConstants.MAX_DIFFICULTY);
    }
}
