package com.chaosbuffalo.mkcore.utils;

public class MathUtils {

    public static float lerp(float v0, float v1, float t){
        return (1.0f - t) * v0 + t * v1;
    }

    public static float clamp(float value, float min, float max){
        return Math.max(Math.min(max, value), min);
    }
}
