package edu.rochester.beetrap.component.player;

import org.bukkit.entity.Entity;

public class HighlightEntityComponent {
    private Entity glowingMinecraftEntity;

    public HighlightEntityComponent() {

    }

    public void setGlowingMinecraftEntity(Entity minecraftEntity) {
        this.glowingMinecraftEntity = minecraftEntity;
    }

    public Entity getGlowingMinecraftEntity() {
        return this.glowingMinecraftEntity;
    }
}
