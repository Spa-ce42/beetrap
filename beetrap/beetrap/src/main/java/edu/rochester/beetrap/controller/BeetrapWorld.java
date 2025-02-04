package edu.rochester.beetrap.controller;

import edu.rochester.beetrap.Main;
import edu.rochester.beetrap.data.BeetrapPlayerData;
import edu.rochester.beetrap.data.GardenEntitiesData;
import edu.rochester.beetrap.model.Flower;
import edu.rochester.beetrap.model.Garden;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class BeetrapWorld implements Listener {
    private static final Logger LOG = LogManager.getLogger(BeetrapWorld.class);
    private final Main main;
    private final World world;
    private final Map<String, BeetrapPlayerData> nameToBeetrapPlayerMap;
    private final Map<String, GardenEntitiesData> nameToGardenEntitiesMap;

    public BeetrapWorld(Main main, World world) {
        this.main = main;
        this.main.getServer().getPluginManager().registerEvents(this, this.main);
        this.world = world;
        this.nameToBeetrapPlayerMap = new HashMap<>();
        this.nameToGardenEntitiesMap = new HashMap<>();
    }

    public void drawQuad(int xCenter, int yCenter, int zCenter,
            int sideToCenterLength, Material material) {
        // Calculate the corner coordinates based in the center and half side length
        int xStart = xCenter - sideToCenterLength - 1;
        int xEnd = xCenter + sideToCenterLength + 1;
        int zStart = zCenter - sideToCenterLength - 1;
        int zEnd = zCenter + sideToCenterLength + 1;

        // Draw the edges of the quadrilateral
        for(int x = xStart; x <= xEnd; x++) {
            world.getBlockAt(x, yCenter, zStart).setType(material); // Top edge
            world.getBlockAt(x, yCenter, zEnd).setType(material);   // Bottom edge
        }
        for(int z = zStart; z <= zEnd; z++) {
            world.getBlockAt(xStart, yCenter, z).setType(material); // Left edge
            world.getBlockAt(xEnd, yCenter, z).setType(material);   // Right edge
        }
    }

    public void spawnFlower(Garden g, Flower f, double x, double y, double z,
            Material material) {
        // Create a Location object with the desired coordinates
        Location location = new Location(world, x, y, z);
        // Spawn the falling block using spawnEntity method
        FallingBlock fallingBlock = world.spawnFallingBlock(location, material.createBlockData());
        fallingBlock.teleport(location);

        // Optional: Set additional properties
        fallingBlock.setGravity(false); // Disable gravity
        fallingBlock.setTicksLived(Integer.MAX_VALUE); // Prevent despawning

        fallingBlock.setMetadata("garden", new FixedMetadataValue(this.main, g.getName()));
        fallingBlock.setMetadata("flower", new FixedMetadataValue(this.main, f.uuid()));

        GardenEntitiesData ged = this.nameToGardenEntitiesMap.computeIfAbsent(g.getName(),
                k -> new GardenEntitiesData());
        ged.addFlower(f.uuid(), fallingBlock);
    }

    public void spawnBeetrapBeeNest(Garden g) {
        Vector bl = g.getTopLeft();
        Vector tr = g.getBottomRight();
        double x = (double)((tr.getBlockX() + bl.getBlockX()) / 2) + 0.5;
        double y = bl.getY() + 1.5;
        double z = (double)((tr.getBlockZ() + bl.getBlockZ()) / 2) + 0.5;
        LOG.info("Spawning a Beetrap bee nest at ({}, {})...", x, z);
        Location location = new Location(this.world, x, y, z);
        ArmorStand as = (ArmorStand)this.world.spawnEntity(location, EntityType.ARMOR_STAND);
        as.setGravity(false);
        as.setInvulnerable(true);
        as.setMarker(true);
        as.setInvisible(true);

        ItemStack beeNestItem = new ItemStack(Material.BEE_NEST, 1);
        as.getEquipment().setHelmet(beeNestItem);

        as.teleport(location);

        GardenEntitiesData ged = this.nameToGardenEntitiesMap.computeIfAbsent(g.getName(),
                k -> new GardenEntitiesData());

        ged.setBeeNest(as);
    }

    public void spawnBeetrapBeeNestAsFallingBlock(Garden g) {
        Vector bl = g.getTopLeft();
        Vector tr = g.getBottomRight();
        int x = (tr.getBlockX() + bl.getBlockX()) / 2;
        int y = bl.getBlockY() + 3;
        int z = (tr.getBlockZ() + bl.getBlockZ()) / 2;
        LOG.info("Spawning a Beetrap bee nest as a falling block at ({}, {})...", x, z);
        Location location = new Location(this.world, x, y, z);
        FallingBlock as = this.world.spawnFallingBlock(location, Material.BEE_NEST.createBlockData());
        as.setGravity(false);
        as.setVelocity(new Vector());
        as.setTicksLived(Integer.MAX_VALUE);
        as.setMetadata("garden", new FixedMetadataValue(this.main, g.getName()));
        as.setMetadata("bee_nest", new FixedMetadataValue(this.main, new Object()));

        GardenEntitiesData ged = this.nameToGardenEntitiesMap.computeIfAbsent(g.getName(),
                k -> new GardenEntitiesData());
        ged.setBeeNest(as);
    }

    public void clearFlowers(String name) {
        GardenEntitiesData ged = this.nameToGardenEntitiesMap.get(name);
        ged.removeFlowers();
    }

    public void destroyBeeNest(String name) {
        GardenEntitiesData ged = this.nameToGardenEntitiesMap.get(name);
        ged.removeBeeNest();
    }

    public World getWorld() {
        return this.world;
    }

    public BeetrapPlayerData getBeetrapPlayerData(Player player) {
        BeetrapPlayerData bpd = this.nameToBeetrapPlayerMap.get(player.getName());

        if(bpd == null) {
            bpd = new BeetrapPlayerData(this.main, player);
            this.nameToBeetrapPlayerMap.put(player.getName(), bpd);
        }

        return bpd;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent pje) {
        Player p = pje.getPlayer();
        BeetrapPlayerData bpd = new BeetrapPlayerData(this.main, p);
        this.nameToBeetrapPlayerMap.put(p.getName(), bpd);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent pqe) {
        Player p = pqe.getPlayer();
        this.nameToBeetrapPlayerMap.remove(p.getName());
    }

    public void drawFlowers(Garden garden, FlowerToMaterialFunction ftmf) {
        double width =
                garden.getBottomRight().getX() - garden.getTopLeft().getX() + 1;
        double length =
                garden.getBottomRight().getZ() - garden.getTopLeft().getZ() + 1;

        for(Flower f : garden.getFlowers().values()) {
            double x = garden.getTopLeft().getX() + width * f.x();
            double z = garden.getTopLeft().getZ() + length * f.z();
            this.spawnFlower(garden, f, x, garden.getTopLeft().getBlockY(),
                    z, ftmf.apply(f));
        }
    }

    public void drawFlower(Garden garden, Flower f, Material material) {
        double width =
                garden.getBottomRight().getX() - garden.getTopLeft().getX() + 1;
        double length =
                garden.getBottomRight().getZ() - garden.getTopLeft().getZ() + 1;
        double x = garden.getTopLeft().getX() + width * f.x();
        double z = garden.getTopLeft().getZ() + length * f.z();
        this.spawnFlower(garden, f, x, garden.getTopLeft().getBlockY(),
                z, material);
    }

    public void spawnParticle(Garden garden, Particle particle, double xNormalized, double zNormalized, int count) {
        double width =
                garden.getBottomRight().getX() - garden.getTopLeft().getX() + 1;
        double length =
                garden.getBottomRight().getZ() - garden.getTopLeft().getZ() + 1;
        double x = garden.getTopLeft().getX() + width * xNormalized;
        double z = garden.getTopLeft().getZ() + length * zNormalized;
        this.world.spawnParticle(particle, x, garden.getTopLeft().getBlockY(), z, count);
    }

    public void constructGarden(String name) {
        this.nameToGardenEntitiesMap.put(name, new GardenEntitiesData());
    }

    public GardenEntitiesData getGardenEntitiesData(String name) {
        return this.nameToGardenEntitiesMap.get(name);
    }
}
