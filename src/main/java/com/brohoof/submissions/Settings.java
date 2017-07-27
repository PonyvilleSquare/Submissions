package com.brohoof.submissions;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;

public class Settings {

    private File plotFile;
    private String plotFileName;
    private final SubmissionsPlugin plugin;
    private File rentFile;
    private String rentFileName;
    private int saveInterval;

    public Settings(final SubmissionsPlugin submissionsPlugin) {
        plugin = submissionsPlugin;
        loadSettings(plugin.getConfig());
    }

    public File getPlotFile() {
        return plotFile;
    }

    public String getPlotFileName() {
        return plotFileName;
    }

    public File getRentFile() {
        return rentFile;
    }

    public String getRentFileName() {
        return rentFileName;
    }

    public int getSaveInterval() {
        return saveInterval;
    }

    private void loadSettings(final FileConfiguration config) {
        rentFileName = config.getString("rentsFile");
        rentFile = new File(plugin.getDataFolder(), rentFileName);
        plotFileName = config.getString("plotFile");
        plotFile = new File(plugin.getDataFolder(), plotFileName);
        saveInterval = config.getInt("save") * 20 * 60;

    }
}
