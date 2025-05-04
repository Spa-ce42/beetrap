package edu.rochester.beetrap.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.util.Vector;

public class Garden {
    private final UUID uuid;
    private final String name;
    private final Vector topLeft, bottomRight;
    private final UUIDToFlowerMap flowers;
    private final Map<UUID, Material> materials;
    private double flowerDiversity;

    public Garden(String name, Vector topLeft, Vector bottomRight) {
        this(UUID.randomUUID(), name, topLeft, bottomRight, new UUIDToFlowerMap());
    }

    public Garden(UUID uuid, String name, Vector topLeft, Vector bottomRight,
            UUIDToFlowerMap flowers) {
        this.uuid = uuid;
        this.name = name;
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
        this.flowers = flowers;
        this.flowerDiversity = -1;
        this.materials = new HashMap<>();
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public Vector getTopLeft() {
        return this.topLeft.clone();
    }

    public Vector getBottomRight() {
        return this.bottomRight.clone();
    }

    public UUIDToFlowerMap getFlowers() {
        return this.flowers;
    }

    public void putFlower(UUID uuid, Flower f) {
        this.flowers.put(uuid, f);
        this.flowerDiversity = -1;
    }

    public Flower getFlower(UUID uuid) {
        return this.flowers.get(uuid);
    }

    public void removeFlower(UUID uuid) {
        this.flowers.remove(uuid);
        this.flowerDiversity = -1;
    }

    public void putFlowerMaterial(UUID uuid, Material material) {
        this.materials.put(uuid, material);
    }

    public Material getMaterial(UUID uuid) {
        return this.materials.get(uuid);
    }

    public void clearFlowers() {
        this.flowers.clear();
        this.flowerDiversity = 0;
    }

    public double getFlowerDiversity() {
        if(this.flowerDiversity < 0) {
            double averageX = 0;
            double averageZ = 0;
            Collection<Flower> flowers = this.flowers.values();
            int n = flowers.size();

            for(Flower f : flowers) {
                averageX = averageX + f.x() * 100;
                averageZ = averageZ + f.z() * 100;
            }

            averageX = averageX / n;
            averageZ = averageZ / n;

            double totalDistance = 0;

            for(Flower f : flowers) {
                double x = f.x() * 100 - averageX;
                x = x * x;
                double z = f.z() * 100 - averageZ;
                z = z * z;
                totalDistance = totalDistance + Math.sqrt(x + z);
            }

            this.flowerDiversity = totalDistance;
        }

        return this.flowerDiversity;
    }
}
