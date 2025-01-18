package edu.rochester.beetrap.model;

import java.util.Objects;
import java.util.UUID;

public final class Flower {
    private final UUID uuid;
    private final double v;
    private final double w;
    private final double x;
    private final double y;
    private final double z;

    public Flower(UUID uuid, double v, double w, double x, double y, double z) {
        this.uuid = uuid;
        this.v = v;
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public UUID uuid() {
        return uuid;
    }

    public double v() {
        return v;
    }

    public double w() {
        return w;
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

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (Flower) obj;
        return Objects.equals(this.uuid, that.uuid) &&
                Double.doubleToLongBits(this.v) == Double.doubleToLongBits(that.v) &&
                Double.doubleToLongBits(this.w) == Double.doubleToLongBits(that.w) &&
                Double.doubleToLongBits(this.x) == Double.doubleToLongBits(that.x) &&
                Double.doubleToLongBits(this.y) == Double.doubleToLongBits(that.y) &&
                Double.doubleToLongBits(this.z) == Double.doubleToLongBits(that.z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, v, w, x, y, z);
    }

    @Override
    public String toString() {
        return "Flower[" +
                "uuid=" + uuid + ", " +
                "v=" + v + ", " +
                "w=" + w + ", " +
                "x=" + x + ", " +
                "y=" + y + ", " +
                "z=" + z + ']';
    }


}
