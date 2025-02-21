package edu.rochester.beetrap.controller;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.api.Results;
import dev.dominion.ecs.api.Results.With1;
import edu.rochester.beetrap.Garden;
import edu.rochester.beetrap.Main;
import edu.rochester.beetrap.component.player.HighlightEntityComponent;
import edu.rochester.beetrap.component.player.IsPollinatingComponent;
import edu.rochester.beetrap.component.player.LookingAtFlowerComponent;
import edu.rochester.beetrap.component.player.PlayerGardenComponent;
import edu.rochester.beetrap.component.player.PlayerComponent;
import edu.rochester.beetrap.system.HighlightEntitySystem;
import edu.rochester.beetrap.system.LookingAtFlowerSystem;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class BeetrapWorld implements Listener {
    private final Main main;
    private final World world;
    private final Dominion dominion;
    private final HighlightEntitySystem highlightEntitySystem;
    private final LookingAtFlowerSystem lookingAtFlowerSystem;

    public BeetrapWorld(Main main, World world) {
        this.main = main;
        this.main.getServer().getPluginManager().registerEvents(this, this.main);
        this.world = world;
        this.dominion = Dominion.create();
        this.highlightEntitySystem = new HighlightEntitySystem(this.main, this.dominion);
        this.lookingAtFlowerSystem = new LookingAtFlowerSystem(this);
    }

    public void registerOnlinePlayersAsEcsEntities() {
        Bukkit.getOnlinePlayers().forEach(this::registerPlayerEcsEntity);
    }

    public void registerPlayerEcsEntity(Player p) {
        PlayerComponent pc = new PlayerComponent(p);

        LookingAtFlowerComponent lafc = new LookingAtFlowerComponent();
        p.setScoreboard(lafc.getScoreboard());

        HighlightEntityComponent hec = new HighlightEntityComponent();

        IsPollinatingComponent ipc = new IsPollinatingComponent();

        this.dominion.createEntity(
                pc,
                lafc,
                hec,
                ipc
        );
    }

    public void deregisterPlayerEcsEntity(Player p) {
        Results<With1<PlayerComponent>> r = this.dominion.findEntitiesWith(PlayerComponent.class);

        for(With1<PlayerComponent> pc : r) {
            if(pc.comp().player().getName().equals(p.getName())) {
                this.dominion.deleteEntity(pc.entity());
            }
        }
    }

    public Entity getPlayerEcsEntity(Player p) {
        Results<With1<PlayerComponent>> r = this.dominion.findEntitiesWith(PlayerComponent.class);

        for(With1<PlayerComponent> pc : r) {
            if(pc.comp().player().getName().equals(p.getName())) {
                return pc.entity();
            }
        }

        return null;
    }

    public void setPlayerGarden(Player player, Garden garden) {
        Entity playerEcsEntity = this.getPlayerEcsEntity(player);
        playerEcsEntity.removeType(PlayerGardenComponent.class);
        playerEcsEntity.add(new PlayerGardenComponent(garden));
    }

    public void setAllPlayerGarden(Garden garden) {
        Results<With1<PlayerComponent>> r = this.dominion.findEntitiesWith(PlayerComponent.class);

        for(With1<PlayerComponent> pc : r) {
            Entity entity = pc.entity();
            entity.removeType(PlayerGardenComponent.class);
            entity.add(new PlayerGardenComponent(garden));
        }
    }

    public Garden getPlayerGarden(Player player) {
        Entity playerEcsEntity = this.getPlayerEcsEntity(player);
        return playerEcsEntity.get(PlayerGardenComponent.class).garden();
    }

    public void highlight(Player player, org.bukkit.entity.Entity targetMinecraftEntity) {
        this.highlightEntitySystem.highlight(player, targetMinecraftEntity);
    }

    public void onPlayerLookAtEntity(Player player, org.bukkit.entity.Entity targetMinecraftEntity) {
        this.lookingAtFlowerSystem.onPlayerLookAtEntity(player, targetMinecraftEntity);
    }

    public World getWorld() {
        return this.world;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent pje) {
        Player p = pje.getPlayer();
        this.registerPlayerEcsEntity(p);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent pqe) {
        Player p = pqe.getPlayer();
        this.deregisterPlayerEcsEntity(p);
    }

    public Dominion getDominion() {
        return this.dominion;
    }
}
