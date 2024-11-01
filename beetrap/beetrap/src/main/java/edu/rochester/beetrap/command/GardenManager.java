package edu.rochester.beetrap.command;

import edu.rochester.beetrap.Garden;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class GardenManager implements CommandExecutor, Listener {
    private final JavaPlugin plugin;
    private final Map<String, Garden> gardens = new HashMap<>();

    public GardenManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private void drawQuad(World world, int xCenter, int yCenter, int zCenter,
            int sideLengthToCenter, Material material) {
        // Calculate the corner coordinates based in the center and half side length
        int xStart = xCenter - sideLengthToCenter - 1;
        int xEnd = xCenter + sideLengthToCenter + 1;
        int zStart = zCenter - sideLengthToCenter - 1;
        int zEnd = zCenter + sideLengthToCenter + 1;

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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label,
            String[] args) {
        if(command.getName().equalsIgnoreCase("garden")) {
            String type = args[0];

            if(type.equalsIgnoreCase("create")) {
                String name = args[1];
                int cx = Integer.parseInt(args[2]);
                int cy = Integer.parseInt(args[3]);
                int cz = Integer.parseInt(args[4]);
                int r = Integer.parseInt(args[5]);

                this.gardens.put(name, new Garden(this.plugin, this, cx, cy, cz, r));
                if(sender instanceof Player p) {
                    drawQuad(p.getWorld(), cx, cy, cz, r,
                            Material.GLASS);
                }
            }

            if(type.equalsIgnoreCase("list")) {
                if(sender instanceof Player p) {
                    for(String s : this.gardens.keySet()) {
                        p.sendMessage(s);
                    }
                }
            }

            if(type.equalsIgnoreCase("delete")) {
                this.gardens.remove(args[1]);
            }

            if(type.equalsIgnoreCase("random")) {
                if(sender instanceof Player p) {
                    String name = args[1];
                    double rate = Double.parseDouble(args[2]);
                    Garden garden = this.gardens.get(name);
                    garden.generateRandomFlowers(rate);
                    garden.placeFlowers(this.plugin, p.getWorld());
                }
            }

            if(type.equalsIgnoreCase("clear")) {
                if(sender instanceof Player p) {
                    String name = args[1];
                    Garden garden = this.gardens.get(name);
                    garden.clear(p.getWorld());
                    garden.placeFlowers(this.plugin, p.getWorld());
                }
            }
        }

        return true;
    }
}
