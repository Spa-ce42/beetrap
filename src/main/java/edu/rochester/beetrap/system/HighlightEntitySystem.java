package edu.rochester.beetrap.system;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Results;
import dev.dominion.ecs.api.Results.With2;
import edu.rochester.beetrap.component.player.HighlightEntityComponent;
import edu.rochester.beetrap.component.player.PlayerComponent;
import fr.skytasul.glowingentities.GlowingEntities;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class HighlightEntitySystem {
    private final Dominion dominion;
    private final GlowingEntities glowingMinecraftEntities;
    
    public HighlightEntitySystem(Plugin plugin, Dominion dominion) {
        this.glowingMinecraftEntities = new GlowingEntities(plugin);
        this.dominion = dominion;
    }

    private HighlightEntityComponent getHighlightEntityComponent(Player player) {
        Results<With2<PlayerComponent, HighlightEntityComponent>> r = this.dominion.findEntitiesWith(
                PlayerComponent.class, HighlightEntityComponent.class);

        for(With2<PlayerComponent, HighlightEntityComponent> w : r) {
            if(w.comp1().player().getName().equals(player.getName())) {
                return w.comp2();
            }
        }

        return null;
    }
    
    public void highlight(Player player, Entity minecraftEntity) {
        HighlightEntityComponent hec = this.getHighlightEntityComponent(player);

        try {
            if(hec.getGlowingMinecraftEntity() == null && minecraftEntity == null) {
                return;
            }

            if(hec.getGlowingMinecraftEntity() == null) {
                hec.setGlowingMinecraftEntity(minecraftEntity);
                this.glowingMinecraftEntities.setGlowing(hec.getGlowingMinecraftEntity(), player);
                return;
            }

            if(minecraftEntity == null) {
                this.glowingMinecraftEntities.unsetGlowing(hec.getGlowingMinecraftEntity(), player);
                hec.setGlowingMinecraftEntity(null);
                return;
            }

            if(hec.getGlowingMinecraftEntity().getEntityId() == minecraftEntity.getEntityId()) {
                return;
            }

            this.glowingMinecraftEntities.unsetGlowing(hec.getGlowingMinecraftEntity(), player);
            hec.setGlowingMinecraftEntity(minecraftEntity);
            this.glowingMinecraftEntities.setGlowing(hec.getGlowingMinecraftEntity(), player);
        } catch(ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }
}
