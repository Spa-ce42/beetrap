package edu.rochester.beetrap.data;

import edu.rochester.beetrap.Main;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.bukkit.event.Listener;

public class DataFolderManager implements Listener {

    @SuppressWarnings("FieldCanBeLocal")
    private final Main main;
    private final File dataFolder;

    public DataFolderManager(Main main) {
        this.main = main;
        this.dataFolder = this.main.getDataFolder();
        this.main.getServer().getPluginManager().registerEvents(this, this.main);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void ensureDataFolderExists() {
        if(!this.dataFolder.exists()) {
            this.dataFolder.mkdirs();
        }
    }

    public File open(String filename) {
        return new File(this.dataFolder, filename);
    }

    public BufferedWriter openW(String filename) {
        try {
            return new BufferedWriter(new FileWriter(this.open(filename)));
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public BufferedReader openR(String filename) {
        try {
            return new BufferedReader(new FileReader(this.open(filename)));
        } catch(FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    public BufferedOutputStream openWb(String filename) {
        try {
            return new BufferedOutputStream(new FileOutputStream(this.open(filename)));
        } catch(FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    public BufferedInputStream openRb(String filename) {
        try {
            return new BufferedInputStream(new FileInputStream(this.open(filename)));
        } catch(FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void onPluginEnable() {
        this.ensureDataFolderExists();
    }

    public void onPluginDisable() {
        this.ensureDataFolderExists();
    }
}
