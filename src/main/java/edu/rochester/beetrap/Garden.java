package edu.rochester.beetrap;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.api.Results;
import dev.dominion.ecs.api.Results.With1;
import dev.dominion.ecs.api.Results.With2;
import edu.rochester.beetrap.component.MinecraftEntityRepresentationComponent;
import edu.rochester.beetrap.component.flower.FlowerValueComponent;
import edu.rochester.beetrap.component.flower.FlowerUuidComponent;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

public class Garden {
    private final String name;
    private final UUID uuid;
    private final Vector topLeft;
    private final Vector bottomRight;
    private final Dominion dominion;
    private org.bukkit.entity.Entity beeNestMinecraftEntity;
    private double flowerDiversity;

    public Garden(String name, Vector topLeft, Vector bottomRight) {
        this.name = name;
        this.uuid = UUID.randomUUID();
        this.topLeft = topLeft.clone();
        this.bottomRight = bottomRight.clone();
        this.dominion = Dominion.create("Garden: " + name);
    }

    public void putFlowerEcsEntity(UUID flowerUuid, double fv, double fw, double fx, double fy, double fz) {
        FlowerUuidComponent uc = new FlowerUuidComponent(flowerUuid);
        FlowerValueComponent v5c = new FlowerValueComponent(fv, fw, fx, fy, fz);
        MinecraftEntityRepresentationComponent merc = new MinecraftEntityRepresentationComponent();
        this.dominion.createEntity(uc, v5c, merc);
        flowerDiversity = -1;
    }

    public UUID putFlowerEcsEntity(double fv, double fw, double fx, double fy, double fz) {
        UUID flowerUuid = UUID.randomUUID();
        this.putFlowerEcsEntity(flowerUuid, fv, fw, fx, fy, fz);
        return flowerUuid;
    }

    public Entity getFlowerEcsEntity(UUID uuid) {
        Results<With1<FlowerUuidComponent>> r = this.dominion.findEntitiesWith(FlowerUuidComponent.class);

        for(With1<FlowerUuidComponent> fuc : r) {
            if(fuc.comp().uuid().equals(uuid)) {
                return fuc.entity();
            }
        }

        return null;
    }

    public void forEachFlowerValue(VoidFunction2<UUID, FlowerValueComponent> f) {
        for(With2<FlowerUuidComponent, FlowerValueComponent> fucafc : this.dominion.findEntitiesWith(FlowerUuidComponent.class, FlowerValueComponent.class)) {
            f.apply(fucafc.comp1().uuid(), fucafc.comp2());
        }
    }

    public void removeFlowerEcsEntity(UUID flowerUuid) {
        Entity flowerEcsEntity = this.getFlowerEcsEntity(flowerUuid);
        MinecraftEntityRepresentationComponent merc = flowerEcsEntity.get(
                MinecraftEntityRepresentationComponent.class);

        org.bukkit.entity.Entity minecraftEntity = merc.getMinecraftEntity();
        if(minecraftEntity != null) {
            minecraftEntity.remove();
        }

        this.dominion.deleteEntity(flowerEcsEntity);
        this.flowerDiversity = -1;
    }

    public void spawnFlowerMinecraftEntity(World world, UUID flowerUuid, double x, double y, double z, Material material) {
        // Create a Location object with the desired coordinates
        Location location = new Location(world, x, y, z);
        // Spawn the falling block using spawnEntity method
        FallingBlock fallingBlock = world.spawnFallingBlock(location, material.createBlockData());
        fallingBlock.teleport(location);

        // Optional: Set additional properties
        fallingBlock.setGravity(false); // Disable gravity
        fallingBlock.setTicksLived(Integer.MAX_VALUE); // Prevent despawning

        Entity flowerEcsEntity = this.getFlowerEcsEntity(flowerUuid);
        MinecraftEntityRepresentationComponent merc = flowerEcsEntity.get(MinecraftEntityRepresentationComponent.class);
        org.bukkit.entity.Entity minecraftEntity = merc.getMinecraftEntity();

        if(minecraftEntity != null) {
            minecraftEntity.remove();
        }

        merc.setMinecraftEntity(fallingBlock);
    }

    public void spawnFlowerMinecraftEntity(World world, UUID flowerUuid, Material material) {
        double width =
                bottomRight.getX() - topLeft.getX() + 1;
        double length =
                bottomRight.getZ() - topLeft.getZ() + 1;
        Entity flowerEcsEntity = this.getFlowerEcsEntity(flowerUuid);
        FlowerValueComponent fvc = flowerEcsEntity.get(FlowerValueComponent.class);
        double x = topLeft.getX() + width * fvc.x();
        double z = topLeft.getZ() + length * fvc.z();

        this.spawnFlowerMinecraftEntity(world, flowerUuid, x, topLeft.getBlockY(), z, material);
    }

    public void spawnFlowerMinecraftEntities(World world, FlowerValueToMaterialFunction f) {
        double width =
                bottomRight.getX() - topLeft.getX() + 1;
        double length =
                bottomRight.getZ() - topLeft.getZ() + 1;

        this.forEachFlowerValue((fUuid, fvc) -> {
            double x = topLeft.getX() + width * fvc.x();
            double z = topLeft.getZ() + length * fvc.z();
            Garden.this.spawnFlowerMinecraftEntity(world, fUuid, x, topLeft.getBlockY(), z, f.apply(fvc.v(), fvc.w(), fvc.x(), fvc.y(), fvc.z()));

        });
    }

    public void generateFlowerEcsEntities(int n) {
        Random r = new Random();

        for(int i = 0; i < n; ++i) {
            this.putFlowerEcsEntity(r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble());
        }
    }

    public void clearFlowerMinecraftEntity(UUID flowerUuid) {
        Entity flowerEcsEntity = this.getFlowerEcsEntity(flowerUuid);
        MinecraftEntityRepresentationComponent merc = flowerEcsEntity.get(
                MinecraftEntityRepresentationComponent.class);
        merc.removeMinecraftEntity();
    }

    public void clearFlowerMinecraftEntities() {
        for(With1<FlowerUuidComponent> fuc : this.dominion.findEntitiesWith(FlowerUuidComponent.class)) {
            MinecraftEntityRepresentationComponent merc = fuc.entity().get(MinecraftEntityRepresentationComponent.class);
            merc.removeMinecraftEntity();
        }
    }

    public org.bukkit.entity.Entity getBeeNestMinecraftEntity() {
        return this.beeNestMinecraftEntity;
    }

    public void spawnBeeNest(World world) {
        int x = (bottomRight.getBlockX() + topLeft.getBlockX()) / 2;
        int y = topLeft.getBlockY() + 3;
        int z = (bottomRight.getBlockZ() + topLeft.getBlockZ()) / 2;
        Location location = new Location(world, x, y, z);
        FallingBlock fallingBeeNest = world.spawnFallingBlock(location, Material.BEE_NEST.createBlockData());
        fallingBeeNest.setGravity(false);
        fallingBeeNest.setVelocity(new Vector());
        fallingBeeNest.setTicksLived(Integer.MAX_VALUE);

        this.beeNestMinecraftEntity = fallingBeeNest;
    }

    public void setBeeNest(org.bukkit.entity.Entity beeMinecraftEntityNest) {
        this.beeNestMinecraftEntity = beeMinecraftEntityNest;
    }

    public org.bukkit.entity.Entity getBeeNest() {
        return this.beeNestMinecraftEntity;
    }

    public void removeBeeNest() {
        this.beeNestMinecraftEntity.remove();
        this.beeNestMinecraftEntity = null;
    }

    public int size() {
        final int[] i = {0};
        this.forEachFlowerValue((value0, value1) -> ++i[0]);
        return i[0];
    }

    public FlowerValueComponent getFlowerValue(UUID flowerUuid) {
        return this.getFlowerEcsEntity(flowerUuid).get(FlowerValueComponent.class);
    }

    public org.bukkit.entity.Entity getFlowerMinecraftEntity(UUID flowerUuid) {
        Results<With1<FlowerUuidComponent>> r = this.dominion.findEntitiesWith(FlowerUuidComponent.class);

        for(With1<FlowerUuidComponent> ibnc : r) {
            if(ibnc.comp().uuid().equals(flowerUuid)) {
                MinecraftEntityRepresentationComponent merc = ibnc.entity()
                        .get(MinecraftEntityRepresentationComponent.class);
                return merc.getMinecraftEntity();
            }
        }

        return null;
    }

    public double getFlowerDiversity() {
        if(this.flowerDiversity < 0) {
            final double[] total = new double[5];
            final int[] n = {0};

            this.forEachFlowerValue((uuid, fvc) -> {
                if(fvc.y() == 0) {
                    return;
                }

                total[0] = total[0] + fvc.v() * 100;
                total[1] = total[1] + fvc.w() * 100;
                total[2] = total[2] + fvc.x() * 100;
                total[3] = total[3] + fvc.y() * 100;
                total[4] = total[4] + fvc.z() * 100;
                ++n[0];
            });

            for(int i = 0; i < total.length; ++i) {
                total[i] = total[i] / n[0];
            }

            double[] totalDistance = new double[1];

            this.forEachFlowerValue((uuid, fvc) -> {
                if(fvc.y() == 0) {
                    return;
                }

                double a = total[0] - fvc.v() * 100;
                double b = total[1] - fvc.w() * 100;
                double c = total[2] - fvc.x() * 100;
                double d = total[3] - fvc.y() * 100;
                double e = total[4] - fvc.z() * 100;
                totalDistance[0] = totalDistance[0] + Math.sqrt(a * a + b * b + c * c + d * d + e * e);
            });

            this.flowerDiversity = totalDistance[0] / n[0];
        }

        return this.flowerDiversity;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof Garden garden)) {
            return false;
        }
        return this.uuid.equals(garden.uuid);
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }

    public Entity getEcsEntityByMinecraftEntity(org.bukkit.entity.Entity minecraftEntity) {
        for(With1<MinecraftEntityRepresentationComponent> merc : this.dominion.findEntitiesWith(MinecraftEntityRepresentationComponent.class)) {
            if(merc.comp().getMinecraftEntity().getEntityId() == minecraftEntity.getEntityId()) {
                return merc.entity();
            }
        }

        return null;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.dominion.toString();
    }
}
