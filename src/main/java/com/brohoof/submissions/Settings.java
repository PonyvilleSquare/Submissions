package com.brohoof.submissions;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;

public class Settings {

    private SubmissionsPlugin plugin;
    private File rentFile;
    private File plotFile;
    private int saveInterval;
    private String rentFileName;
    private String plotFileName;

    public Settings(SubmissionsPlugin submissionsPlugin) {
        plugin = submissionsPlugin;
        loadSettings(plugin.getConfig());
    }

    private void loadSettings(FileConfiguration config) {
        rentFileName = config.getString("rentsFile");
        rentFile = new File(plugin.getDataFolder(), rentFileName);
        plotFileName = config.getString("plotFile");
        plotFile = new File(plugin.getDataFolder(), plotFileName);
        saveInterval = config.getInt("save") * 20 * 60;

    }

    public File getRentFile() {
        return rentFile;
    }

    public File getPlotFile() {
        return plotFile;
    }

    public int getSaveInterval() {
        return saveInterval;
    }

    public String getRentFileName() {
        return rentFileName;
    }

    public String getPlotFileName() {
        return plotFileName;
    }
}
