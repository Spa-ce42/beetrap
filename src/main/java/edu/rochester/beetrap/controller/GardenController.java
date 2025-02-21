package edu.rochester.beetrap.controller;

import edu.rochester.beetrap.Game;
import edu.rochester.beetrap.Main;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;

public class GardenController implements CommandExecutor, Listener {
    private final Main main;
    private final BeetrapWorld beetrapWorld;
    private final TabCompleter tc;
    private Game game;

    public GardenController(Main main) {
        this.main = main;
        this.beetrapWorld = new BeetrapWorld(main, Bukkit.getWorlds().getFirst());
        this.main.getServer().getPluginManager().registerEvents(this, this.main);
        this.tc = new CommandTabCompleter();
        this.main.getCommand("game").setExecutor(this);
        this.main.getCommand("game").setTabCompleter(this.tc);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label,
            String[] args) {
        try {
            if(command.getName().equalsIgnoreCase("game")) {
                if(args[0].equalsIgnoreCase("new")) {
                    this.beetrapWorld.registerOnlinePlayersAsEcsEntities();
                    this.game = new Game(this.main, this.beetrapWorld, "Garden");
                    return true;
                }
            }
        } catch(Throwable t) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            sender.sendMessage(ChatColor.RED + sw.toString());
            t.printStackTrace();
            return false;
        }

        return false;
    }
}
