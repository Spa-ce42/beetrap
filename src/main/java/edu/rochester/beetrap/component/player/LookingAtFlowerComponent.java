package edu.rochester.beetrap.component.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class LookingAtFlowerComponent {
    private final Scoreboard scoreboard;
    private final Objective lookingAtFlowerObjective;
    private UUID flowerUuid;
    private final Map<String, Score> entryToLookingAtFlowerScoreMap;

    @SuppressWarnings("DataFlowIssue")
    public LookingAtFlowerComponent() {
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.lookingAtFlowerObjective = this.scoreboard.registerNewObjective("flower", "dummy", "Flower");
        this.lookingAtFlowerObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.entryToLookingAtFlowerScoreMap = new HashMap<>();
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public UUID getFlowerUuid() {
        return flowerUuid;
    }

    public void setFlowerUuid(UUID uuid) {
        this.flowerUuid = uuid;
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

    public void setLookingAtFlower(UUID flowerUuid) {
        this.flowerUuid = flowerUuid;
    }
}
