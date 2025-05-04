package edu.rochester.beetrap.controller;

import edu.rochester.beetrap.Main;
import edu.rochester.beetrap.data.BeetrapPlayerData;
import edu.rochester.beetrap.data.GardenEntitiesData;
import edu.rochester.beetrap.model.Flower;
import edu.rochester.beetrap.model.Garden;
import edu.rochester.beetrap.service.GardenService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import javax.naming.ldap.LdapContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Score;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class Game implements Listener {
    private static final Material POLLINATE_ACTION_MATERIAL = Material.BEE_NEST;
    private final Main main;
    private final GardenService gs;
    private final BeetrapWorld bw;
    private final String gardenPrefix;
    private final List<Garden> gardens;
    private final List<GardenEntitiesData> gardenEntitiesData;
    private int turn;
    private static final Logger LOG = LogManager.getLogger(Game.class);
    private final Random r = new Random();
    private static final Material[] FLOWER_MATERIALS = {
            Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID, Material.AZURE_BLUET,
            Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP,
            Material.OXEYE_DAISY, Material.LILY_OF_THE_VALLEY, Material.TORCHFLOWER, Material.PINK_PETALS
    };
    private Material pollinatedFlowerMaterial;
    private final FlowerToMaterialFunction ftmf = (g, f) -> {
        if(g.getMaterial(f.uuid()) != null) {
            return g.getMaterial(f.uuid());
        }

        if(f.y() <= 0) {
            return Material.WITHER_ROSE;
        }

        if(Math.abs(f.y() - 0.5) <= 0.1) {
            return Material.MANGROVE_PROPAGULE;
        }

        if(this.diversifiedRanking) {
            int i = this.r.nextInt(FLOWER_MATERIALS.length);
            return FLOWER_MATERIALS[i];
        }

        if(Game.this.turn == 0) {
            int i = this.r.nextInt(FLOWER_MATERIALS.length);
            return FLOWER_MATERIALS[i];
        }

        return this.pollinatedFlowerMaterial;
    };

    private List<Flower> newFlowers;

    private boolean diversifiedRanking;
    private boolean largeRadius;

    public Game(Main main, GardenService gs, BeetrapWorld bw, String gardenPrefix) {
        this.main = main;
        this.gs = gs;
        this.bw = bw;
        this.gardenPrefix = gardenPrefix;
        this.gardens = new ArrayList<>();
        this.gardenEntitiesData = new ArrayList<>();

        this.gs.createGarden(this.gardenPrefix + "_" + this.turn, new Vector(-10, 0, -10), new Vector(10, 0, 10));
        Garden g = this.gs.getGarden(this.gardenPrefix + "_" + this.turn);
        this.gardens.add(g);
        this.gs.generateFlowers(this.gardenPrefix + "_" + this.turn, 20);
        this.bw.drawFlowers(g, ftmf);
        this.bw.spawnBeetrapBeeNestAsFallingBlock(g);

        this.main.getServer().getPluginManager().registerEvents(this, this.main);

        this.gardenEntitiesData.add(this.bw.getGardenEntitiesData(g.getName()));
    }

    private record DistanceAndFlower(double distance, Flower f) implements Comparable<DistanceAndFlower> {
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
            return Double.compare(distance, that.distance) == 0 && Objects.equals(f,
                    that.f);
        }

        @Override
        public int hashCode() {
            return Objects.hash(distance, f);
        }
    }

    /**
     * Spawns flowers close to a particular flower f in terms of the x and z values of f.
     * The new flowers will be spawned within a ring of the f.
     * @param f the original flower
     * @param n number of new flowers
     * @param inr the inner radius that defines the ring, normalized to [0, 1]
     * @param onr the outer radius, normalized
     * @return the list of new flowers
     */
    private List<Flower> spawnFlowersCloseTo(Flower f, int n, double inr, double onr) {
        List<Flower> flowers = new ArrayList<>();
        for(int i = 0; i < n; ++i) {
            double r = this.r.nextDouble(inr, onr);
            double theta = this.r.nextDouble(0, Math.TAU);
            Flower flower = new Flower(UUID.randomUUID(), this.r.nextDouble(), this.r.nextDouble(),r * Math.cos(theta) + f.x(), 1, r * Math.sin(theta) + f.z());
            flowers.add(flower);
        }

        flowers.sort((f1, f2) -> {
            double df1 = Math.sqrt(
                    (f.x() - f1.x()) * (f.x() - f1.x()) + (f.z() - f1.z()) * (f.z() - f1.z()));
            double df2 = Math.sqrt(
                    (f.x() - f2.x()) * (f.x() - f2.x()) + (f.z() - f2.z()) * (f.z() - f2.z()));

            int d = (int)(df1 - df2);

            if(d != 0) {
                return d;
            }

            if(df1 - df2 > 0) {
                return 1;
            } else if(df1 - df2 < 0) {
                return -1;
            }

            return 0;
        });

        return flowers;
    }

    private void pollinate(Flower f) {
        Garden g = this.gardens.get(this.turn);
        Flower[] h = g.getFlowers().values().toArray(new Flower[0]);
        int amountOfFlowersToKill = this.diversifiedRanking ? 1 : 5;
        Queue<DistanceAndFlower> flowersToKill = new PriorityQueue<>(
                (o1, o2) -> -o1.compareTo(o2));
        GardenEntitiesData ged = this.gardenEntitiesData.get(this.turn);

        for(Flower i : h) {
            if(i.y() <= 0) {
                continue;
            }

            double x2 = f.x() - i.x();
            x2 = x2 * x2;
            double z2 = f.z() - i.z();
            z2 = z2 * z2;
            double distance = Math.sqrt(x2 + z2);
            LOG.info("{}", distance);
            flowersToKill.add(new DistanceAndFlower(distance, i));
        }

        ++this.turn;
        this.gs.createGarden(this.gardenPrefix + "_" + this.turn, g.getTopLeft(), g.getBottomRight());
        Garden j = this.gs.getGarden(this.gardenPrefix + "_" + this.turn);
        this.gardens.add(j);

        for(Flower flower : g.getFlowers().values()) {
            j.putFlower(flower.uuid(), flower);
        }

        this.bw.constructGarden(j.getName());
        GardenEntitiesData gee = this.bw.getGardenEntitiesData(j.getName());
        this.gardenEntitiesData.add(gee);

        for(Entry<UUID, Entity> e : ged.getFlowers().entrySet()) {
            gee.addFlower(e.getKey(), e.getValue());
        }

        gee.setBeeNest(ged.getBeeNest());

        for(int i = 0; i < amountOfFlowersToKill; ++i) {
            DistanceAndFlower daf = flowersToKill.poll();

            if(daf == null) {
                break;
            }

            UUID uuid = daf.f().uuid();
            gee.removeFlower(uuid);

            Flower deadFlower = new Flower(uuid, daf.f.v(), daf.f.w(), daf.f.x(), 0, daf.f.z());
            j.putFlower(uuid, deadFlower);
            this.bw.drawFlower(j, daf.f, Material.WITHER_ROSE);
        }


        int i = 0;
        for(Flower newFlower : this.newFlowers) {
            if(!this.diversifiedRanking) {
                if(i >= 3) {
                    ++i;
                    continue;
                }
            } else {
                if(!(5 < i && i < 9)) {
                    ++i;
                    continue;
                }
            }

            j.putFlower(newFlower.uuid(), newFlower);
            this.bw.drawFlower(j, newFlower, this.ftmf.apply(g, newFlower));
            ++i;
        }

        this.gardenEntitiesData.get(this.turn - 1).removeEntities();
    }

    private void onPlayerPollinate(Player p) {
        BeetrapPlayerData bpd = this.bw.getBeetrapPlayerData(p);

        if(bpd.getIsPollinating().get()) {
            return;
        }

        Entity e = bpd.getGlowingEntity();

        if(e == null) {
            return;
        }

        Location eLocation = e.getLocation();
        Garden g = this.gardens.get(this.turn);
        GardenEntitiesData ged = this.bw.getGardenEntitiesData(g.getName());
        Entity f = ged.getBeeNest();
        Location fLocation = f.getLocation();
        Vector v = eLocation.toVector().subtract(fLocation.toVector());
        v.setY(0);
        v.multiply(1.0 / 60);

        BukkitTask bt = Bukkit.getScheduler().runTaskTimerAsynchronously(this.main,
                () -> f.setVelocity(v), 0, 0);

        bpd.getIsPollinating().set(true);

        FallingBlock fb = (FallingBlock)e;
        this.pollinatedFlowerMaterial = fb.getBlockData().getMaterial();

        Bukkit.getScheduler().runTaskLater(this.main, () -> {
            bt.cancel();
            f.setVelocity(new Vector());
            eLocation.setY(fLocation.getY());
            f.teleport(eLocation);

            Flower originalFlower = g.getFlower((UUID)e.getMetadata("flower").getFirst().value());
            this.newFlowers = Game.this.spawnFlowersCloseTo(originalFlower, 30, 0.01, this.largeRadius ? 0.3 : 0.2);

            int i = 0, j = 1, k = 3;
            for(Flower bud : this.newFlowers) {
                if(!this.diversifiedRanking) {
                    if(i < 3) {
                        this.bw.drawBud(g, bud, this.ftmf.apply(g,
                                new Flower(bud.uuid(), bud.v(), bud.w(), bud.x(), 0.5,
                                        bud.z())), String.valueOf(j++));
                    }
                    this.bw.drawBud(g, bud, this.ftmf.apply(g,
                                    new Flower(bud.uuid(), bud.v(), bud.w(), bud.x(), 0.5, bud.z())),
                            "");
                } else {
                    if(5 < i && i < 9) {
                        this.bw.drawBud(g, bud, this.ftmf.apply(g,
                                new Flower(bud.uuid(), bud.v(), bud.w(), bud.x(), 0.5,
                                        bud.z())), String.valueOf(k--));
                    }
                    this.bw.drawBud(g, bud, this.ftmf.apply(g,
                                    new Flower(bud.uuid(), bud.v(), bud.w(), bud.x(), 0.5, bud.z())),
                            "");
                }

                ++i;
            }

            BukkitTask spawnParticleCircleTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.main, () -> {
                Game.this.bw.spawnParticleCircle(Particle.FALLING_HONEY, eLocation.getX(), eLocation.getY(), eLocation.getZ(), this.largeRadius ? 3 : 1.5, 0.05);
            }, 0, 20);

            Bukkit.getScheduler().runTaskLater(this.main, () -> {
                spawnParticleCircleTask.cancel();
                Game.this.pollinate(originalFlower);
                bpd.getIsPollinating().set(false);
            }, 300);

        }, 60);
    }

    private void setPlayerInventory(Player p, int index, Material material, int amount, String displayName) {
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

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent pie) {
        Player p = pie.getPlayer();
        Material itemInMainHandMaterial = p.getInventory().getItemInMainHand().getType();

        if(pie.getAction() == Action.RIGHT_CLICK_AIR || pie.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(itemInMainHandMaterial == POLLINATE_ACTION_MATERIAL) {
                this.onPlayerPollinate(p);
                this.setPlayerInventory(p, 0, Material.CLOCK, 1, "Back");
                this.setPlayerInventory(p, 2, Material.LADDER, 1, "Change Ranking");
                this.setPlayerInventory(p, 6, Material.STRING, 1, "Change Pollination Radius");
                this.setPlayerInventory(p, 8, Material.CLOCK, 1, "Forward");

                return;
            }

            if(itemInMainHandMaterial == Material.LADDER) {
                int i = p.getInventory().getHeldItemSlot();
                if(i == 2) {
                    this.diversifiedRanking = !this.diversifiedRanking;
                    p.sendMessage("Diversified rankings " + (this.diversifiedRanking ? "enabled" : "disabled") + "!");
                }
            }

            if(itemInMainHandMaterial == Material.STRING) {
                int i = p.getInventory().getHeldItemSlot();

                if(i == 6) {
                    this.largeRadius = !this.largeRadius;
                    p.sendMessage("Pollination radius " + (this.diversifiedRanking ? "enlarged" : "shrank") + "!");
                }
            }

            if(itemInMainHandMaterial == Material.CLOCK) {
                int i = p.getInventory().getHeldItemSlot();

                if(i == 0) {
                    if(this.turn == 0) {
                        p.sendMessage("You are looking at the zeroth garden already!");
                        return;
                    }

                    GardenEntitiesData ged = this.gardenEntitiesData.get(this.turn);
                    ged.removeFlowers();
                    ged.removeBeeNest();
                    --this.turn;

                    Garden g = this.gardens.get(this.turn);
                    this.bw.drawFlowers(g, ftmf);
                    this.bw.spawnBeetrapBeeNestAsFallingBlock(g);
                    return;
                }

                if(i == 8) {
                    if(this.turn == this.gardens.size() - 1) {
                        p.sendMessage("You are looking at the newest garden already!");
                        return;
                    }

                    GardenEntitiesData ged = this.gardenEntitiesData.get(this.turn);
                    ged.removeFlowers();
                    ged.removeBeeNest();
                    ++this.turn;

                    Garden g = this.gardens.get(this.turn);
                    this.bw.drawFlowers(g, ftmf);
                    this.bw.spawnBeetrapBeeNestAsFallingBlock(g);
                    return;
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
        Entity entity = this.getTargetEntity(e.getPlayer(), 3);
        BeetrapPlayerData bpd = this.bw.getBeetrapPlayerData(e.getPlayer());
        bpd.setGlowing(entity);
        if(entity != null) {
            try {
                List<MetadataValue> mw = entity.getMetadata("flower");
                UUID uuid = (UUID)mw.getFirst().value();
                Garden garden = this.gardens.get(this.turn);
                String gardenName = garden.getName();
                bpd.resetLookingAtFlowerScores();
                Score flowerOwner = bpd.getLookingAtFlowerScore("Garden: " + gardenName);
                flowerOwner.setScore(7);
                Score flowerDiversity = bpd.getLookingAtFlowerScore(String.format("Flower Diversity: %.2f", garden.getFlowerDiversity()));
                flowerDiversity.setScore(6);
                Flower f = garden.getFlower(uuid);
                Score flowerV = bpd.getLookingAtFlowerScore(String.format("v: %.2f", f.v()));
                flowerV.setScore(5);
                Score flowerW = bpd.getLookingAtFlowerScore(String.format("w: %.2f", f.w()));
                flowerW.setScore(4);
                Score flowerX = bpd.getLookingAtFlowerScore(String.format("x: %.2f", f.x()));
                flowerX.setScore(3);
                Score flowerY = bpd.getLookingAtFlowerScore(String.format("y: %.2f", f.y()));
                flowerY.setScore(2);
                Score flowerZ = bpd.getLookingAtFlowerScore(String.format("z: %.2f", f.z()));
                flowerZ.setScore(1);

                bpd.setLookingAtFlower(garden, f);

                Player p = bpd.getPlayer();
                Inventory i = p.getInventory();
                ItemStack is = i.getItem(4);

                if(is == null) {
                    is = new ItemStack(POLLINATE_ACTION_MATERIAL, 1);
                    ItemMeta im = is.getItemMeta();
                    im.setDisplayName("Pollinate");
                    is.setItemMeta(im);
                    i.setItem(4, is);
                } else if(is.getType() != POLLINATE_ACTION_MATERIAL) {
                    is.setType(POLLINATE_ACTION_MATERIAL);
                    is.setAmount(1);
                    ItemMeta im = is.getItemMeta();
                    im.setDisplayName("Pollinate");
                    is.setItemMeta(im);
                    i.setItem(4, is);
                }

                return;
            } catch(NoSuchElementException f) {
                // If it's not our flower, then we don't care about it.
            }
        }

        Player p = bpd.getPlayer();
        Inventory i = p.getInventory();
        i.setItem(4, null);
    }

    public void destroy() {
        for(Garden g : this.gardens) {
            String name = g.getName();
            this.bw.clearFlowers(name);
            this.bw.destroyBeeNest(name);
            this.gs.destroyGarden(name);
        }
    }
}
