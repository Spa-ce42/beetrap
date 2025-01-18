package edu.rochester.beetrap.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import edu.rochester.beetrap.Main;
import edu.rochester.beetrap.data.DataFolderManager;
import edu.rochester.beetrap.data.FlowerDeserializer;
import edu.rochester.beetrap.data.FlowerSerializer;
import edu.rochester.beetrap.data.GardenDeserializer;
import edu.rochester.beetrap.data.GardenSerializer;
import edu.rochester.beetrap.data.VectorDeserializer;
import edu.rochester.beetrap.data.VectorSerializer;
import edu.rochester.beetrap.event.OnPluginDisableCallback;
import edu.rochester.beetrap.event.OnPluginEnableCallback;
import edu.rochester.beetrap.model.Flower;
import edu.rochester.beetrap.model.Garden;
import edu.rochester.beetrap.repository.NameToGardenMap;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class GardenService implements OnPluginEnableCallback, OnPluginDisableCallback {
    private final Main main;
    private NameToGardenMap gardens;
    private final ObjectMapper om;

    public GardenService(Main main) {
        this.main = main;
        this.main.registerOnPluginEnableCallback(this);
        this.main.registerOnPluginDisableCallback(this);
        this.gardens = new NameToGardenMap();
        this.om = new ObjectMapper();
        SimpleModule sm = new SimpleModule();
        sm.addSerializer(Vector.class, new VectorSerializer());
        sm.addSerializer(Garden.class, new GardenSerializer());
        sm.addSerializer(Flower.class, new FlowerSerializer());
        sm.addDeserializer(Vector.class, new VectorDeserializer());
        sm.addDeserializer(Garden.class, new GardenDeserializer());
        sm.addDeserializer(Flower.class, new FlowerDeserializer());
        this.om.registerModule(sm);
    }

    public boolean createGarden(String name, Vector topLeft, Vector topRight) {
        if(gardens.containsKey(name)) {
            return false;
        }

        Garden g = new Garden(name, topLeft, topRight);
        this.gardens.put(name, g);
        return true;
    }

    public String gardensToString() throws JsonProcessingException {
        return this.om.writeValueAsString(this.gardens);
    }

    public String[] listGardenNames() {
        return this.gardens.keySet().toArray(new String[0]);
    }

    public void destroyGarden(String name) {
        if(!this.gardens.containsKey(name)) {
            return;
        }

        this.gardens.remove(name);
    }

    public void destroyAllGardens() {
        this.gardens.clear();
    }

    public void saveGardens(String filename) {
        DataFolderManager dfm = this.main.getDataFolderManager();
        try {
            this.om.writeValue(dfm.open(filename), this.gardens);
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void loadGardens(String filename) {
        DataFolderManager dfm = this.main.getDataFolderManager();
        File f = dfm.open(filename);
        if(!f.exists()) {
            this.gardens = new NameToGardenMap();
        }

        try {
            this.gardens = this.om.readValue(f, NameToGardenMap.class);
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void generateFlowers(String gardenName, int n) {
        Garden g = this.gardens.get(gardenName);
        Random r = new Random();
        for(int i = 0; i < n; ++i) {
            Flower f = new Flower(UUID.randomUUID(), r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble());
            g.putFlower(f.uuid(), f);
        }
    }

    public int gardenCount() {
        return this.gardens.size();
    }

    public Garden getGarden(String name) {
        return this.gardens.get(name);
    }

    public Flower[] getFlowers(String gardenName) {
        return this.gardens.get(gardenName).getFlowers().values().toArray(new Flower[0]);
    }

    @Override
    public void onPluginDisable() {
        this.saveGardens("gardens.json");
    }

    @Override
    public void onPluginEnable() {
        this.loadGardens("gardens.json");
    }
}
