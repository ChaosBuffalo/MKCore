package com.chaosbuffalo.mkcore.utils;

import com.chaosbuffalo.mkcore.GameConstants;
import net.minecraft.util.math.vector.Vector3d;

public class ProjectileUtils {

    public static class BallisticResult {
        public Vector3d lowArc;
        public Vector3d highArc;
        public boolean hasHighArc;
        public boolean foundSolution;

        public BallisticResult(Vector3d lowArc, Vector3d highArc){
            this.lowArc = lowArc;
            this.highArc = highArc;
            this.hasHighArc = true;
            this.foundSolution = true;
        }

        public BallisticResult(Vector3d lowArc){
            this.lowArc = lowArc;
            this.hasHighArc = false;
            this.foundSolution = true;
        }

        public BallisticResult(){
            this.foundSolution = false;
            this.hasHighArc = false;
        }
    }

    public static BallisticResult solveBallisticArcStationaryTarget(Vector3d projPos, Vector3d target,
                                                                    float tickVelocity, float tickGravity) {
        Vector3d diff = target.subtract(projPos);
        Vector3d diffXZ = new Vector3d(diff.x, 0.0, diff.z);
        double groundDist = diffXZ.length();

        double vel = tickVelocity * GameConstants.TICKS_PER_SECOND;
        double seconds = groundDist / vel;
        double heightLostToGravity = tickGravity * GameConstants.TICKS_PER_SECOND * seconds;

        double yDiff = diff.y;
        double yWithGravity = yDiff + heightLostToGravity;

        Vector3d targetPos = new Vector3d(diff.getX(), yWithGravity, diff.getZ());
        return new BallisticResult(targetPos);
    }
}
