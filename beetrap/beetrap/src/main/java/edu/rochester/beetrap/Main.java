package edu.rochester.beetrap;

import edu.rochester.beetrap.controller.GardenController;
import edu.rochester.beetrap.data.DataFolderManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin implements Listener {
    private DataFolderManager dataFolderManager;
    private GardenController gardenManager;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.dataFolderManager = new DataFolderManager(this);
        this.gardenManager = new GardenController(this);

        this.dataFolderManager.onPluginEnable();
        this.gardenManager.onPluginEnable();
    }

    public DataFolderManager getDataFolderManager() {
        return this.dataFolderManager;
    }

    @Override
    public void onDisable() {
        this.gardenManager.onPluginDisable();
        this.dataFolderManager.onPluginDisable();
    }
}
