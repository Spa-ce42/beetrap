package edu.rochester.beetrap.controller;

import edu.rochester.beetrap.Main;
import edu.rochester.beetrap.model.Garden;
import edu.rochester.beetrap.service.GardenService;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

public class GardenController implements CommandExecutor, Listener {
    private final Main main;
    private final GardenService gs;
    private final BeetrapWorld bw;
    private final TabCompleter tc;
    private Game game;

    public GardenController(Main main) {
        this.main = main;
        this.gs = new GardenService(main);
        this.bw = new BeetrapWorld(main, Bukkit.getWorlds().getFirst());
        this.main.getServer().getPluginManager().registerEvents(this, this.main);
        this.main.getCommand("draw_quad").setExecutor(this);
        this.main.getCommand("garden").setExecutor(this);
        this.tc = new CommandTabCompleter(this.gs);
        this.main.getCommand("garden").setTabCompleter(this.tc);
        this.main.getCommand("game").setExecutor(this);
        this.main.getCommand("game").setTabCompleter(this.tc);
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

                    this.bw.drawQuad(centerX, centerY, centerZ, sideToCenterLength,
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
                        boolean b = gs.createGarden(name, new Vector(topLeftX, topLeftY, topLeftZ),
                                new Vector(bottomRightX, bottomRightY, bottomRightZ));

                        if(b) {
                            p.sendMessage(
                                    "Successfully created a garden with name: \"" + name + "\".");
                        } else {
                            p.sendMessage(ChatColor.RED + "A garden named: \"" + name
                                    + "\" already exists.");
                        }

                        this.bw.constructGarden(name);

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
                        p.sendMessage(
                                "Successfully generated " + n + " flowers in the garden named: \""
                                        + name + "\".");
                        return true;
                    }

                    if(args[0].equalsIgnoreCase("save")) {
                        String filename = args[1];
                        p.sendMessage(
                                "Saving " + this.gs.gardenCount() + " gardens into \"" + filename
                                        + "\"...");
                        long l = System.nanoTime();
                        this.gs.saveGardens(filename);
                        long m = System.nanoTime();
                        p.sendMessage("Saving complete. Took " + ((double)(m - l) / 1000000000.)
                                + " seconds.");
                        return true;
                    }

                    if(args[0].equalsIgnoreCase("load")) {
                        String filename = args[1];
                        p.sendMessage("Loading gardens from \"" + filename + "\"...");
                        this.gs.loadGardens(filename);

                        for(String s : this.gs.getGardenNames()) {
                            this.bw.constructGarden(s);
                        }
                        p.sendMessage("Loading complete.");
                        return true;
                    }

                    if(args[0].equalsIgnoreCase("draw_flowers")) {
                        String gardenName = args[1];
                        Garden garden = this.gs.getGarden(gardenName);
                        this.bw.drawFlowers(garden, (f) -> Material.POPPY);
                        return true;
                    }

                    if(args[0].equalsIgnoreCase("clear_flowers")) {
                        String gardenName = args[1];
                        this.bw.clearFlowers(gardenName);

                        return true;
                    }

                    if(args[0].equalsIgnoreCase("remove_flowers")) {
                        String gardenName = args[1];
                        Garden g = this.gs.getGarden(gardenName);
                        g.clearFlowers();

                        return true;
                    }

                    if(args[0].equalsIgnoreCase("spawn_bee_nest")) {
                        String gardenName = args[1];
                        Garden g = this.gs.getGarden(gardenName);
                        this.bw.spawnBeetrapBeeNestAsFallingBlock(g);

                        return true;
                    }

                    if(args[0].equalsIgnoreCase("destroy_bee_nest")) {
                        String gardenName = args[1];
                        this.bw.destroyBeeNest(gardenName);

                        return true;
                    }
                }

                if(command.getName().equalsIgnoreCase("game")) {
                    if(args[0].equalsIgnoreCase("new")) {
                        if(this.game == null) {
                            this.game = new Game(this.main, this.gs, this.bw, "Garden");
                        }

                        return true;
                    }

                    if(args[0].equalsIgnoreCase("destroy")) {
                        if(this.game != null) {
                            this.game.destroy();
                            this.game = null;
                        }

                        return true;
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

    public void onPluginDisable() {
        this.gs.saveGardens("gardens.json");
    }

    public void onPluginEnable() {
        this.gs.loadGardens("gardens.json");
    }
}
