package edu.rochester.beetrap.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Entity;

public class GardenEntitiesData {
    private final Map<UUID, Entity> flowers;
    private Entity beeNest;

    public GardenEntitiesData() {
        this.flowers = new HashMap<>();
    }

    public boolean addFlower(UUID uuid, Entity e) {
        if(this.flowers.containsKey(uuid)) {
            return false;
        }

        this.flowers.put(uuid, e);
        return true;
    }

    public Entity getFlower(UUID uuid) {
        return this.flowers.get(uuid);
    }

    public boolean removeFlower(UUID uuid) {
        Entity e = this.flowers.remove(uuid);

        if(e == null) {
            return false;
        }

        e.remove();
        return true;
    }

    public void setBeeNest(Entity entity) {
        if(this.beeNest != null) {
            this.removeBeeNest();
        }

        this.beeNest = entity;
    }

    public Entity getBeeNest() {
        return this.beeNest;
    }

    public void removeBeeNest() {
        if(this.beeNest == null) {
            return;
        }

        this.beeNest.remove();
        this.beeNest = null;
    }

    public void removeFlowers() {
        for(Entity e : this.flowers.values()) {
            e.remove();
        }

        this.flowers.clear();
    }

    public Map<UUID, Entity> getFlowers() {
        return Collections.unmodifiableMap(this.flowers);
    }
}
