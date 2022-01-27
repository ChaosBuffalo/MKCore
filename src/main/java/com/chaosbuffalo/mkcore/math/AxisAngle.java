package com.chaosbuffalo.mkcore.math;

import com.chaosbuffalo.mkcore.utils.MathUtils;
import net.minecraft.util.math.vector.Vector3d;

public class AxisAngle extends Vector3d {
    protected double angle;

    public AxisAngle(double angle, double xIn, double yIn, double zIn) {
        super(xIn, yIn, zIn);
        this.angle = (angle < 0.0 ? Math.PI + Math.PI + angle % (Math.PI + Math.PI) : angle) % (Math.PI + Math.PI);
    }

    public double getAngle() {
        return angle;
    }

    public Vector3d transform(Vector3d vector) {
        double sin = Math.sin(angle);
        double cos = MathUtils.cosFromSin(sin, angle);
        double dot = x * vector.getX() + y * vector.getY() + z * vector.getZ();
        return new Vector3d(
                vector.getX() * cos + sin * (y * vector.getZ() - z * vector.getY()) + (1.0 - cos) * dot * x,
                vector.getY() * cos + sin * (z * vector.getX() - x * vector.getZ()) + (1.0 - cos) * dot * y,
                vector.getZ() * cos + sin * (x * vector.getY() - y * vector.getX()) + (1.0 - cos) * dot * z
        );
    }
}
