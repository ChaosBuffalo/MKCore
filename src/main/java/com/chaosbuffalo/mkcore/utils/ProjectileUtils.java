package com.chaosbuffalo.mkcore.utils;

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
                                                                    float velocity, float gravity) {


        Vector3d diff = target.subtract(projPos);
        Vector3d diffXZ = new Vector3d(diff.x, 0.0, diff.z);
        double groundDist = diffXZ.length();
        float vel2 = velocity*velocity;
        float vel4 = velocity*velocity*velocity*velocity;
        double y = diff.y;
        double gx = gravity * groundDist;
        double root = vel4 - gravity*((gravity* groundDist * groundDist) + (2*y*vel2));
        if (root < 0){
            return new BallisticResult();
        }

        root = Math.sqrt(root);
        double lowAng = Math.atan2(vel2 - root, gx);
        double highAng = Math.atan2(vel2 + root, gx);
        Vector3d heading = diffXZ.normalize();
        Vector3d lowArc = heading.scale(Math.cos(lowAng)*velocity)
                .add(new Vector3d(0.0, 1.0, 0.0)
                        .scale(Math.sin(lowAng)*velocity));
        if (lowAng != highAng){
            Vector3d highArc = heading.scale(Math.cos(highAng)*velocity)
                    .add(new Vector3d(0.0, 1.0, 0.0)
                            .scale(Math.sin(highAng)*velocity));
            return new BallisticResult(lowArc, highArc);
        } else {
            return new BallisticResult(lowArc);
        }
    }
}
