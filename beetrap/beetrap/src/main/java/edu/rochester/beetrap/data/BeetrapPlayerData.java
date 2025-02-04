package edu.rochester.beetrap.data;

import edu.rochester.beetrap.Main;
import edu.rochester.beetrap.model.Flower;
import edu.rochester.beetrap.model.Garden;
import fr.skytasul.glowingentities.GlowingEntities;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class BeetrapPlayerData {
    private final Player player;
    private final Scoreboard scoreboard;
    private final Objective lookingAtFlowerObjective;
    private Garden gardenInQuestion;
    private Flower lookingAtFlower;
    private final Map<String, Score> entryToLookingAtFlowerScoreMap;
    private final GlowingEntities ge;
    private Entity glowingEntity;
    private AtomicBoolean isPollinating;

    public BeetrapPlayerData(Main main, Player player) {
        this.ge = new GlowingEntities(main);
        this.player = player;
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.lookingAtFlowerObjective = this.scoreboard.registerNewObjective("flower", "dummy", "Flower");
        this.lookingAtFlowerObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.player.setScoreboard(this.scoreboard);
        this.entryToLookingAtFlowerScoreMap = new HashMap<>();
        this.isPollinating = new AtomicBoolean(false);
    }

    public Player getPlayer() {
        return this.player;
    }

    public GlowingEntities getGlowingEntities() {
        return this.ge;
    }

    public Score getLookingAtFlowerScore(String entry) {
        if(this.entryToLookingAtFlowerScoreMap.containsKey(entry)) {
            return this.entryToLookingAtFlowerScoreMap.get(entry);
        }

        Score score = this.lookingAtFlowerObjective.getScore(entry);
        this.entryToLookingAtFlowerScoreMap.put(entry, score);
        return score;
    }

    public void resetLookingAtFlowerScores() {
        for(String entry : this.entryToLookingAtFlowerScoreMap.keySet()) {
            this.scoreboard.resetScores(entry);
        }

        this.entryToLookingAtFlowerScoreMap.clear();
    }

    public void setGlowing(Entity e) {
        try {
            if(this.glowingEntity == null && e == null) {
                return;
            }

            if(this.glowingEntity == null) {
                this.glowingEntity = e;
                this.ge.setGlowing(this.glowingEntity, this.player);
                return;
            }

            if(e == null) {
                this.ge.unsetGlowing(this.glowingEntity, this.player);
                this.glowingEntity = null;
                return;
            }

            if(this.glowingEntity.getEntityId() == e.getEntityId()) {
                return;
            }

            this.ge.unsetGlowing(this.glowingEntity, this.player);
            this.glowingEntity = e;
            this.ge.setGlowing(this.glowingEntity, this.player);
        } catch(ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void setLookingAtFlower(Garden g, Flower f) {
        this.gardenInQuestion = g;
        this.lookingAtFlower = f;
    }

    public Entity getGlowingEntity() {
        return this.glowingEntity;
    }

    public Garden getGardenInQuestion() {
        return this.gardenInQuestion;
    }

    public Flower getLookingAtFlower() {
        return this.lookingAtFlower;
    }

    public AtomicBoolean getIsPollinating() {
        return this.isPollinating;
    }
}
