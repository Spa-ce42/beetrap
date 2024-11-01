package edu.rochester.beetrap;

import edu.rochester.beetrap.command.GardenManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.BlockIterator;

public class Garden implements Listener {
    private final JavaPlugin jp;
    private final GardenManager gm;
    private final Flower[][] slots;
    private final Map<UUID, Flower> flowers;
    private int leftX;
    private int y;
    private int bottomZ;
    private final int sideLength;
    Scoreboard scoreboard;
    Objective objective;


    public Garden(JavaPlugin jp, GardenManager gm, int centerX, int centerY, int centerZ, int sideLengthToCenter) {
        this.jp = jp;
        this.gm = gm;
        this.sideLength = (sideLengthToCenter << 1) + 1;
        this.slots = new Flower[this.sideLength][this.sideLength];
        this.flowers = new HashMap<>();
        this.leftX = centerX - sideLengthToCenter;
        this.y = centerY;
        this.bottomZ = centerZ - sideLengthToCenter;
        this.jp.getServer().getPluginManager().registerEvents(this, this.jp);

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        scoreboard = manager.getNewScoreboard();
        objective = scoreboard.registerNewObjective("flower_values", "dummy", "Flower Values");
    }

    public void generateRandomFlowers(double rate) {
        Random r = new Random();

        for(int i = 0; i < this.sideLength; ++i) {
            for(int j = 0; j < this.sideLength; ++j) {
                if(r.nextDouble() < rate) {
                    this.slots[i][j] = new Flower(null, r.nextDouble(), r.nextDouble(), r.nextDouble(),
                            r.nextDouble(), r.nextDouble());
                }
            }
        }
    }

    public void placeFlowers(JavaPlugin plugin, World world) {
        for(int i = 0; i < this.sideLength; ++i) {
            for(int j = 0; j < this.sideLength; ++j) {
                Flower flower = this.slots[i][j];

                if(flower != null) {
                    double a = flower.getA();
                    Material m;
                    if(0 <= a && a <= 0.2) {
                        m = Material.RED_TULIP;
                    } else if(0.2 <= a && a <= 0.4) {
                        m = Material.ORANGE_TULIP;
                    } else if(0.4 <= a && a <= 0.6) {
                        m = Material.PINK_TULIP;
                    } else {
                        m = Material.WHITE_TULIP;
                    }

                    world.getBlockAt(this.leftX + i, this.y, this.bottomZ + j).setType(m);
                    ArmorStand as = world.spawn(new Location(world, this.leftX + i + 0.5, this.y, this.bottomZ + j + 0.5), ArmorStand.class);
                    as.addScoreboardTag("flower");
                    as.setMetadata("flower_uuid", new FixedMetadataValue(plugin, flower.getUUID().toString()));
                    flower.setArmorStand(as);
                    as.setInvisible(true);
                    as.setInvulnerable(true);
                    this.flowers.put(flower.getUUID(), flower);
                } else {
                    world.getBlockAt(this.leftX + i, this.y, this.bottomZ + j)
                            .setType(Material.AIR);
                }
            }
        }
    }

    public void clear(World world) {
        for(int i = 0; i < this.sideLength; ++i) {
            for(int j = 0; j < this.sideLength; ++j) {
                this.slots[i][j] = null;
            }
        }

        for(Entity e : world.getEntitiesByClass(ArmorStand.class)) {
            if(e.getMetadata("flower_uuid") != null) {
                e.remove();
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent pme) {
        Player p = pme.getPlayer();
        List<Entity> nearbyE = p.getNearbyEntities(4.5, 4.5, 4.5);
        List<Entity> entities = new ArrayList<>(nearbyE);

        Entity target = null;
        BlockIterator bItr = new BlockIterator(p, 4);
        Block block;
        Location loc;
        int bx, by, bz;
        double ex, ey, ez;

        while(bItr.hasNext()) {
            block = bItr.next();
            bx = block.getX();
            by = block.getY();
            bz = block.getZ();

            for(Entity e : entities) {
                loc = e.getLocation();
                ex = loc.getX();
                ey = loc.getY();
                ez = loc.getZ();
                if((bx - .75 <= ex && ex <= bx + 1.75) && (bz - .75 <= ez && ez <= bz + 1.75) && (
                        by - 1 <= ey && ey <= by + 2.5)) {
                    target = e;
                    break;
                }
            }
        }

        if(target != null) {
            UUID uuid = UUID.fromString(target.getMetadata("flower_uuid").getFirst().asString());
            Flower f = this.flowers.get(uuid);
            p.sendMessage("a: " + f.getA());
            p.sendMessage("b: " + f.getB());
            p.sendMessage("c: " + f.getC());
            p.sendMessage("d: " + f.getD());
            p.sendMessage("e: " + f.getE());
            return;
        }

        objective.setDisplaySlot(null);
        p.setScoreboard(scoreboard);
    }
}
