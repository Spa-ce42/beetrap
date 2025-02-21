package edu.rochester.beetrap;

import edu.rochester.beetrap.component.flower.FlowerValueComponent;
import edu.rochester.beetrap.component.player.IsPollinatingComponent;
import edu.rochester.beetrap.component.player.LookingAtFlowerComponent;
import edu.rochester.beetrap.controller.BeetrapWorld;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class Game implements Listener {
    private static final Material POLLINATE_ACTION_MATERIAL = Material.BEE_NEST;
    private final Main main;
    private final BeetrapWorld beetrapWorld;
    private final String gardenPrefix;
    private final List<Garden> gardens;
    private int turn;
    private final World world;

    private Material flowerValueToMaterialFunction(double v, double w, double x, double y, double z) {
        return y == 0 ? Material.WITHER_ROSE : Material.POPPY;
    }

    public Game(Main main, BeetrapWorld beetrapWorld, String gardenPrefix) {
        this.main = main;
        this.gardenPrefix = gardenPrefix;
        this.beetrapWorld = beetrapWorld;
        this.gardens = new ArrayList<>();
        this.world = beetrapWorld.getWorld();

        Garden g = new Garden(this.gardenPrefix + "_" + this.turn,
                new Vector(-10, 0, -10), new Vector(10, 0, 10));
        this.gardens.add(g);
        g.generateFlowerEcsEntities(20);
        g.spawnFlowerMinecraftEntities(this.world, this::flowerValueToMaterialFunction);
        g.spawnBeeNest(this.world);
        this.beetrapWorld.setAllPlayerGarden(g);
        this.main.getServer().getPluginManager().registerEvents(this, this.main);
    }

    private Garden newTurn() {
        Garden currentGarden = this.gardens.get(this.turn);
        ++this.turn;
        Garden newGarden = new Garden(this.gardenPrefix + "_" + this.turn, new Vector(-10, 0, -10), new Vector(10, 0, 10));
        this.gardens.add(newGarden);
        currentGarden.forEachFlowerValue((iu, fd) -> {
            newGarden.putFlowerEcsEntity(iu, fd.v(), fd.w(), fd.x(), fd.y(), fd.z());
        });
        this.beetrapWorld.setAllPlayerGarden(newGarden);
        newGarden.spawnFlowerMinecraftEntities(this.world, this::flowerValueToMaterialFunction);
        newGarden.setBeeNest(currentGarden.getBeeNest());
        currentGarden.setBeeNest(null);
        currentGarden.clearFlowerMinecraftEntities();
        return newGarden;
    }

    private void pollinate(UUID flowerUuid) {
        Garden currentGarden = this.gardens.get(this.turn);
        int amountOfFlowersToKill = (int)Math.ceil(currentGarden.size() * 0.15);
        Queue<DistanceAndFlower> flowersToKill = new PriorityQueue<>(
                (o1, o2) -> -o1.compareTo(o2));
        FlowerValueComponent fc = currentGarden.getFlowerValue(flowerUuid);

        currentGarden.forEachFlowerValue((iu, ic) -> {
            if(ic.y() <= 0) {
                return;
            }

            double x2 = fc.x() - ic.x();
            x2 = x2 * x2;
            double z2 = fc.z() - ic.z();
            z2 = z2 * z2;
            double distance = Math.sqrt(x2 + z2);
            flowersToKill.add(new DistanceAndFlower(distance, iu));
        });

        Garden newGarden = this.newTurn();

        for(int i = 0; i < amountOfFlowersToKill; ++i) {
            DistanceAndFlower daf = flowersToKill.poll();

            if(daf == null) {
                break;
            }

            UUID uuid = daf.flowerUuid;

            FlowerValueComponent fe = newGarden.getFlowerValue(uuid);
            newGarden.removeFlowerEcsEntity(uuid);

            UUID newUuid = newGarden.putFlowerEcsEntity(fe.v(), fe.w(), fe.x(), 0, fe.z());
            newGarden.spawnFlowerMinecraftEntity(this.world, newUuid, Material.WITHER_ROSE);
        }

        Random random = new Random();
        for(int i = 0; i < amountOfFlowersToKill; ++i) {
            double r = random.nextDouble(0.01, 0.1);
            double theta = random.nextDouble(0, Math.TAU);

            UUID newFlowerUuid = newGarden.putFlowerEcsEntity(random.nextDouble(),
                    random.nextDouble(), r * Math.cos(theta) + fc.x(), 1,
                    r * Math.sin(theta) + fc.z());
            newGarden.spawnFlowerMinecraftEntity(this.world, newFlowerUuid, Material.POPPY);
        }
    }

    private void onPlayerPollinate(Player p) {
        Garden garden = this.gardens.get(this.turn);
        dev.dominion.ecs.api.Entity playerEcsEntity = this.beetrapWorld.getPlayerEcsEntity(p);
        IsPollinatingComponent ipc = playerEcsEntity.get(IsPollinatingComponent.class);
        LookingAtFlowerComponent lafc = playerEcsEntity.get(LookingAtFlowerComponent.class);

        if(ipc.isPollinating()) {
            return;
        }

        UUID fUuid = lafc.getFlowerUuid();

        if(fUuid == null) {
            return;
        }

        Location eLocation = garden.getFlowerMinecraftEntity(fUuid).getLocation();
        Entity f = garden.getBeeNestMinecraftEntity();
        Location fLocation = f.getLocation();
        Vector v = eLocation.toVector().subtract(fLocation.toVector());
        v.setY(0);
        v.multiply(1.0 / 60);

        BukkitTask bt = Bukkit.getScheduler().runTaskTimerAsynchronously(this.main,
                () -> f.setVelocity(v), 0, 0);

        ipc.setIsPollinating(true);

        Bukkit.getScheduler().runTaskLater(this.main, () -> {
            bt.cancel();
            f.setVelocity(new Vector());
            eLocation.setY(fLocation.getY());
            f.teleport(eLocation);
            ipc.setIsPollinating(false);
            Game.this.pollinate(fUuid);
        }, 60);
    }

    private void setPlayerInventory(Player p, int index, Material material, int amount,
            String displayName) {
        Inventory i = p.getInventory();
        ItemStack is = i.getItem(index);

        if(is == null) {
            is = new ItemStack(material, amount);
            ItemMeta im = is.getItemMeta();
            im.setDisplayName(displayName);
            is.setItemMeta(im);
            i.setItem(index, is);
        } else if(is.getType() != material) {
            is.setType(material);
            is.setAmount(amount);
            ItemMeta im = is.getItemMeta();
            im.setDisplayName(displayName);
            is.setItemMeta(im);
            i.setItem(index, is);
        }
    }

    private void previousGarden() {
        Garden garden = this.gardens.get(this.turn);
        garden.clearFlowerMinecraftEntities();
        garden.removeBeeNest();
        --this.turn;

        Garden ogGarden = this.gardens.get(this.turn);
        ogGarden.spawnFlowerMinecraftEntities(this.beetrapWorld.getWorld(), this::flowerValueToMaterialFunction);
        ogGarden.spawnBeeNest(this.world);
        this.beetrapWorld.setAllPlayerGarden(ogGarden);
    }

    private void nextGarden() {
        Garden garden = this.gardens.get(this.turn);
        garden.clearFlowerMinecraftEntities();
        garden.removeBeeNest();
        ++this.turn;

        Garden ngGarden = this.gardens.get(this.turn);
        ngGarden.spawnFlowerMinecraftEntities(this.beetrapWorld.getWorld(), this::flowerValueToMaterialFunction);
        ngGarden.spawnBeeNest(this.world);
        this.beetrapWorld.setAllPlayerGarden(ngGarden);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent pie) {
        Player p = pie.getPlayer();
        Material itemInMainHandMaterial = p.getInventory().getItemInMainHand().getType();

        if(pie.getAction() == Action.RIGHT_CLICK_AIR
                || pie.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(itemInMainHandMaterial == POLLINATE_ACTION_MATERIAL) {
                this.onPlayerPollinate(p);
                this.setPlayerInventory(p, 0, Material.CLOCK, 1, "Back");
                this.setPlayerInventory(p, 8, Material.CLOCK, 1, "Forward");

                return;
            }

            if(itemInMainHandMaterial == Material.CLOCK) {
                int i = p.getInventory().getHeldItemSlot();

                if(i == 0) {
                    if(this.turn == 0) {
                        p.sendMessage("You are looking at the zeroth garden already!");
                        return;
                    }

                    this.previousGarden();
                    return;
                }

                if(i == 8) {
                    if(this.turn == this.gardens.size() - 1) {
                        p.sendMessage("You are looking at the newest garden already!");
                        return;
                    }

                    this.nextGarden();
                }
            }
        }
    }

    public Entity getTargetEntity(Player player, int r) {
        for(Entity entity : player.getNearbyEntities(r, r, r)) {
            Location eye = player.getEyeLocation();
            Vector toEntity = entity.getLocation().toVector().subtract(eye.toVector());
            double dot = toEntity.normalize().dot(eye.getDirection());
            if(dot > 0.983) {
                return entity;
            }
        }
        return null;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Entity targetMinecraftEntity = this.getTargetEntity(player, 3);
        this.beetrapWorld.highlight(player, targetMinecraftEntity);

        if(targetMinecraftEntity == null) {
            Inventory i = player.getInventory();
            i.setItem(4, null);
            return;
        }

        try {
            this.beetrapWorld.onPlayerLookAtEntity(player, targetMinecraftEntity);

            Inventory playerInventory = player.getInventory();
            ItemStack playerInventoryMiddleSlot = playerInventory.getItem(4);

            if(playerInventoryMiddleSlot == null) {
                playerInventoryMiddleSlot = new ItemStack(POLLINATE_ACTION_MATERIAL, 1);
                ItemMeta im = playerInventoryMiddleSlot.getItemMeta();
                im.setDisplayName("Pollinate");
                playerInventoryMiddleSlot.setItemMeta(im);
                playerInventory.setItem(4, playerInventoryMiddleSlot);
            } else if(playerInventoryMiddleSlot.getType() != POLLINATE_ACTION_MATERIAL) {
                playerInventoryMiddleSlot.setType(POLLINATE_ACTION_MATERIAL);
                playerInventoryMiddleSlot.setAmount(1);
                ItemMeta im = playerInventoryMiddleSlot.getItemMeta();
                im.setDisplayName("Pollinate");
                playerInventoryMiddleSlot.setItemMeta(im);
                playerInventory.setItem(4, playerInventoryMiddleSlot);
            }

        } catch(NoSuchElementException f) {
            // If it's not our flower, then we don't care about it.
        }
    }

    public Garden getCurrentGarden() {
        return this.gardens.get(this.turn);
    }

    private record DistanceAndFlower(double distance, UUID flowerUuid) implements
            Comparable<DistanceAndFlower> {

        @Override
        public int compareTo(@NotNull DistanceAndFlower o) {
            return (int)Math.ceil((this.distance - o.distance) * 1000);
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) {
                return true;
            }
            if(!(o instanceof DistanceAndFlower that)) {
                return false;
            }
            return Double.compare(distance, that.distance) == 0 && Objects.equals(flowerUuid,
                    that.flowerUuid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(distance, flowerUuid);
        }
    }
}
