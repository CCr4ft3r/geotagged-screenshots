package com.ccr4ft3r.geotaggedscreenshots.container;

import net.minecraft.world.phys.Vec3;

import java.io.Serializable;

public class Vec3S implements Serializable {

    private final double x;
    private final double y;
    private final double z;

    public Vec3S(Vec3 vec3) {
        this(vec3.x(), vec3.y(), vec3.z());
    }

    public Vec3S(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }
}