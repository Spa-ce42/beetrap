package edu.rochester.beetrap.controller;

import edu.rochester.beetrap.Main;
import edu.rochester.beetrap.model.Flower;
import edu.rochester.beetrap.model.Garden;
import edu.rochester.beetrap.service.GardenService;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class GardenController implements CommandExecutor, Listener {
    private final Main main;
    private final GardenService gs;

    public GardenController(Main main) {
        this.main = main;
        this.gs = new GardenService(main);
    }

    public void drawQuad(World world, int xCenter, int yCenter, int zCenter,
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

    public void spawnFallingBlock(Garden g, Flower flower, World world, double x, double y, double z, Material material) {
        // Create a Location object with the desired coordinates
        Location location = new Location(world, x, y, z);

        // Spawn the falling block using spawnEntity method
        FallingBlock fallingBlock = world.spawnFallingBlock(location, material.createBlockData());
        fallingBlock.teleport(location);

        // Optional: Set additional properties
        fallingBlock.setGravity(false); // Disable gravity
        fallingBlock.setTicksLived(Integer.MAX_VALUE); // Prevent despawning

        fallingBlock.setMetadata("garden", new FixedMetadataValue(this.main, g.getName()));
        fallingBlock.setMetadata("flower", new FixedMetadataValue(this.main, flower.uuid()));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label,
            String[] args) {
        System.out.println(sender.getClass());
        try {
            if(sender instanceof Player p) {
                if(command.getName().equalsIgnoreCase("draw_quad")) {
                    int centerX = Integer.parseInt(args[0]);
                    int centerY = Integer.parseInt(args[1]);
                    int centerZ = Integer.parseInt(args[2]);
                    int sideToCenterLength = Integer.parseInt(args[3]);

                    this.drawQuad(p.getWorld(), centerX, centerY, centerZ, sideToCenterLength,
                            Material.GLASS);
                    return true;
                }

                if(command.getName().equalsIgnoreCase("garden")) {
                    if(args[0].equalsIgnoreCase("create")) {
                        String name = args[1];
                        int topLeftX = Integer.parseInt(args[2]);
                        int topLeftY = Integer.parseInt(args[3]);
                        int topLeftZ = Integer.parseInt(args[4]);
                        int bottomRightX = Integer.parseInt(args[5]);
                        int bottomRightY = Integer.parseInt(args[6]);
                        int bottomRightZ = Integer.parseInt(args[7]);
                        boolean b = gs.createGarden(name, new Vector(topLeftX, topLeftY, topLeftZ), new Vector(bottomRightX, bottomRightY, bottomRightZ));

                        if(b) {
                            p.sendMessage(
                                    "Successfully created a garden with name: \"" + name + "\".");
                        } else {
                            p.sendMessage(ChatColor.RED + "A garden named: \"" + name + "\" already exists.");
                        }

                        return true;
                    }

                    if(args[0].equalsIgnoreCase("list_gardens_in_detail")) {
                        p.sendMessage(this.gs.gardensToString());
                        return true;
                    }

                    if(args[0].equalsIgnoreCase("list")) {
                        String[] s = this.gs.listGardenNames();

                        if(s.length == 0) {
                            p.sendMessage(ChatColor.GRAY + "(The list is empty)");
                            return true;
                        }

                        for(String t : s) {
                            p.sendMessage(t);
                        }

                        return true;
                    }

                    if(args[0].equalsIgnoreCase("destroy")) {
                        String name = args[1];
                        this.gs.destroyGarden(name);
                            p.sendMessage(
                                    "Successfully removed the garden named: \"" + name + "\".");
                        return true;
                    }

                    if(args[0].equalsIgnoreCase("destroy_all")) {
                        this.gs.destroyAllGardens();
                        p.sendMessage("Successfully destroyed all gardens.");
                        return true;
                    }

                    if(args[0].equalsIgnoreCase("generate_flowers")) {
                        String name = args[1];
                        int n = Integer.parseInt(args[2]);
                        this.gs.generateFlowers(name, n);
                        p.sendMessage("Successfully generated " + n + " flowers in the garden named: \"" + name + "\".");
                        return true;
                    }

                    if(args[0].equalsIgnoreCase("save")) {
                        p.sendMessage("Saving " + this.gs.gardenCount() + " gardens...");
                        long l = System.nanoTime();
                        this.gs.saveGardens("gardens.json");
                        long m = System.nanoTime();
                        p.sendMessage("Saving complete. Took " + ((double)(m - l) / 1000000000.) + " seconds.");
                        return true;
                    }

                    if(args[0].equalsIgnoreCase("load")) {
                        p.sendMessage("Loading gardens...");
                        this.gs.loadGardens("gardens.json");
                        p.sendMessage("Loading complete.");
                        return true;
                    }

                    if(args[0].equalsIgnoreCase("draw_flowers")) {
                        String gardenName = args[1];
                        Garden garden = this.gs.getGarden(gardenName);
                        Flower[] flowers = this.gs.getFlowers(gardenName);
                        World world = p.getWorld();
                        double width = garden.getBottomRight().getX() - garden.getTopLeft().getX() + 1;
                        double length = garden.getBottomRight().getZ() - garden.getTopLeft().getZ() + 1;

                        for(Flower f : flowers) {
                            double x = garden.getTopLeft().getX() + width * f.x();
                            double z = garden.getTopLeft().getZ() + length * f.z();
                            p.sendMessage(String.format("Spawned a flower at (%f, %f)", x, z));
                            this.spawnFallingBlock(garden, f, world, x, garden.getTopLeft().getBlockY(), z, Material.POPPY);
                        }
                    }
                }
            }
        } catch(Throwable t) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            sender.sendMessage(ChatColor.RED + sw.toString());
            return false;
        }

        return false;
    }
}
