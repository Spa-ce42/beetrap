package edu.rochester.beetrap.component;

import org.bukkit.entity.Entity;

public class MinecraftEntityRepresentationComponent {
    private Entity minecraftEntity;

    public MinecraftEntityRepresentationComponent() {

    }

    public Entity getMinecraftEntity() {
        return minecraftEntity;
    }

    public void setMinecraftEntity(Entity minecraftEntity) {
        this.minecraftEntity = minecraftEntity;
    }

    public void removeMinecraftEntity() {
        if(this.minecraftEntity != null) {
            this.minecraftEntity.remove();
            this.minecraftEntity = null;
        }
    }
}
