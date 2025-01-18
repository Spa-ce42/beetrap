package edu.rochester.beetrap.model;

import java.util.UUID;
import org.bukkit.util.Vector;

public class Garden {
    private final UUID uuid;
    private final String name;
    private final Vector topLeft, bottomRight;
    private final UUIDToFlowerMap flowers;

    public Garden(String name, Vector topLeft, Vector bottomRight) {
        this(UUID.randomUUID(), name, topLeft, bottomRight, new UUIDToFlowerMap());
    }

    public Garden(UUID uuid, String name, Vector topLeft, Vector bottomRight, UUIDToFlowerMap flowers) {
        this.uuid = uuid;
        this.name = name;
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
        this.flowers = flowers;
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
    }
}
