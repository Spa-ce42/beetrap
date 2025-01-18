package edu.rochester.beetrap;

import edu.rochester.beetrap.controller.GardenController;
import edu.rochester.beetrap.data.DataFolderManager;
import edu.rochester.beetrap.event.OnPluginDisableCallback;
import edu.rochester.beetrap.event.OnPluginEnableCallback;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin implements Listener {
    private DataFolderManager dataFolderManager;
    private GardenController gardenManager;
    private List<OnPluginEnableCallback> onPluginEnableCallbackList;
    private List<OnPluginDisableCallback> onPluginDisableCallbackList;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.onPluginEnableCallbackList = new ArrayList<>();
        this.onPluginDisableCallbackList = new ArrayList<>();

        this.dataFolderManager = new DataFolderManager(this);
        this.gardenManager = new GardenController(this);
        this.getCommand("draw_quad").setExecutor(this.gardenManager);
        this.getCommand("garden").setExecutor(this.gardenManager);

        for(OnPluginEnableCallback onPluginEnableCallback : this.onPluginEnableCallbackList) {
            onPluginEnableCallback.onPluginEnable();
        }
    }

    public void registerOnPluginEnableCallback(OnPluginEnableCallback onPluginEnableCallback) {
        this.onPluginEnableCallbackList.add(onPluginEnableCallback);
    }

    public void registerOnPluginDisableCallback(OnPluginDisableCallback onPluginDisableCallback) {
        this.onPluginDisableCallbackList.add(onPluginDisableCallback);
    }

    public DataFolderManager getDataFolderManager() {
        return this.dataFolderManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent pje) {
        pje.setJoinMessage("Welcome to Beetrap: Minecraft Edition!");
    }

    @Override
    public void onDisable() {
        for(OnPluginDisableCallback onPluginDisableCallback : this.onPluginDisableCallbackList) {
            onPluginDisableCallback.onPluginDisable();
        }
    }
}
