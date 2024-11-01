package edu.rochester.beetrap;

import edu.rochester.beetrap.command.GardenManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin implements Listener {
    private GardenManager gardenManager;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.gardenManager = new GardenManager(this);
        this.getCommand("garden").setExecutor(this.gardenManager);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent pje) {
        pje.setJoinMessage("Welcome to Beetrap: Minecraft Edition!");
    }

    @Override

    public void onDisable() {

    }
}
