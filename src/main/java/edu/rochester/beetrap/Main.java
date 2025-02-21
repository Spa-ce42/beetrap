package edu.rochester.beetrap;

import edu.rochester.beetrap.controller.GardenController;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin implements Listener {
    private GardenController gardenManager;

    @Override
    public void onEnable() {
        System.setProperty("dominion.logging-level", "OFF");
        this.getServer().getPluginManager().registerEvents(this, this);
        this.gardenManager = new GardenController(this);
    }

    @Override
    public void onDisable() {

    }
}
