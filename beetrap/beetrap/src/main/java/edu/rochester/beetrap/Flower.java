package edu.rochester.beetrap;

import java.util.UUID;
import org.bukkit.entity.ArmorStand;

public class Flower {
    private UUID uuid;
    private double a, b, c, d, e;
    private ArmorStand as;

    public Flower(ArmorStand as, double a, double b, double c, double d, double e) {
        this.uuid = UUID.randomUUID();
        this.as = as;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public ArmorStand getArmorStand() {
        return this.as;
    }

    public void setArmorStand(ArmorStand as) {
        this.as = as;
    }

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double getC() {
        return c;
    }

    public void setC(double c) {
        this.c = c;
    }

    public double getD() {
        return d;
    }

    public void setD(double d) {
        this.d = d;
    }

    public double getE() {
        return e;
    }

    public void setE(double e) {
        this.e = e;
    }
}
