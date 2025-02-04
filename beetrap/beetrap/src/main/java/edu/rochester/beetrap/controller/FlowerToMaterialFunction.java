package edu.rochester.beetrap.controller;

import edu.rochester.beetrap.model.Flower;
import org.bukkit.Material;

public interface FlowerToMaterialFunction {
    Material apply(Flower f);
}
