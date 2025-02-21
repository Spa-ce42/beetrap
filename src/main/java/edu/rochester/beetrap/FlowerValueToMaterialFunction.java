package edu.rochester.beetrap;

import org.bukkit.Material;

@FunctionalInterface
public interface FlowerValueToMaterialFunction {
    Material apply(double v, double w, double x, double y, double z);
}
