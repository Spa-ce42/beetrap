package edu.rochester.beetrap.controller;

import edu.rochester.beetrap.model.Flower;
import edu.rochester.beetrap.model.Garden;
import org.bukkit.Material;

public interface FlowerToMaterialFunction {
    Material apply(Garden g, Flower f);
}
