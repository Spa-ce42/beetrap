package edu.rochester.beetrap.system;

import edu.rochester.beetrap.Garden;
import edu.rochester.beetrap.component.flower.FlowerValueComponent;
import edu.rochester.beetrap.component.flower.FlowerUuidComponent;
import edu.rochester.beetrap.component.player.LookingAtFlowerComponent;
import edu.rochester.beetrap.controller.BeetrapWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Score;

public class LookingAtFlowerSystem {
    private final BeetrapWorld beetrapWorld;

    public LookingAtFlowerSystem(BeetrapWorld beetrapWorld) {
        this.beetrapWorld = beetrapWorld;
    }

    public void onPlayerLookAtEntity(Garden garden, Player player, Entity targetMinecraftEntity) {
        dev.dominion.ecs.api.Entity playerEcsEntity = this.beetrapWorld.getPlayerEcsEntity(player);
        dev.dominion.ecs.api.Entity targetEcsEntity = garden.getEcsEntityByMinecraftEntity(targetMinecraftEntity);

        if(targetEcsEntity == null || !targetEcsEntity.has(FlowerValueComponent.class)) {
            return;
        }

        LookingAtFlowerComponent lafc = playerEcsEntity.get(LookingAtFlowerComponent.class);
        lafc.resetLookingAtFlowerScores();

        FlowerValueComponent f = targetEcsEntity.get(FlowerValueComponent.class);
        Score flowerOwner = lafc.getLookingAtFlowerScore("Garden: " + garden.getName());
        flowerOwner.setScore(7);
        Score flowerDiversity = lafc.getLookingAtFlowerScore(
                String.format("Flower Diversity: %.2f", garden.getFlowerDiversity()));
        flowerDiversity.setScore(6);
        Score flowerV = lafc.getLookingAtFlowerScore(String.format("v: %.2f", f.v()));
        flowerV.setScore(5);
        Score flowerW = lafc.getLookingAtFlowerScore(String.format("w: %.2f", f.w()));
        flowerW.setScore(4);
        Score flowerX = lafc.getLookingAtFlowerScore(String.format("x: %.2f", f.x()));
        flowerX.setScore(3);
        Score flowerY = lafc.getLookingAtFlowerScore(String.format("y: %.2f", f.y()));
        flowerY.setScore(2);
        Score flowerZ = lafc.getLookingAtFlowerScore(String.format("z: %.2f", f.z()));
        flowerZ.setScore(1);

        lafc.setLookingAtFlower(targetEcsEntity.get(FlowerUuidComponent.class).uuid());
    }
}
