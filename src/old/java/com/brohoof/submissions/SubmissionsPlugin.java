package com.brohoof.submissions;

import java.io.File;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class SubmissionsPlugin extends JavaPlugin {
    private BuilderListener eventListener;
    private boolean fullyLoaded;
    private Permission permission;
    private PlotManager plotManager;
    private File plotsFile;
    private File rentsFile;
    private WorldEditPlugin worldEdit;

    String getMessage(final String line, final Object... args) {
        String oline = getConfig().getString("messages." + line);
        if (oline == null)
            oline = line;
        return ChatColor.translateAlternateColorCodes('&', String.format(oline, args));
    }

    Permission getPermission() {
        return permission;
    }

    PlotManager getPlotManager() {
        return plotManager;
    }

    WorldEditPlugin getWorldEdit() {
        return worldEdit;
    }

    boolean isAdmin(final Player player) {
        return player.hasPermission("submissions.admin");
    }

    @Override
    public void onDisable() {
        saveRents();
    }

    @Override
    public void onEnable() {
        eventListener = new BuilderListener(this);
        getServer().getPluginManager().registerEvents(eventListener, this);
        getCommand("subm").setExecutor(new SubmCommand(this));
        plotsFile = new File(getDataFolder(), "plots.yml");
        rentsFile = new File(getDataFolder(), "rents.yml");
        permission = new Permission(this);
        if (!plotsFile.exists())
            try {
                plotsFile.createNewFile();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        if (!rentsFile.exists())
            try {
                rentsFile.createNewFile();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        try {
            worldEdit = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
        } catch (final Throwable e) {
            e.printStackTrace();
        }
        reload(true);
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
                saveRents();
            }
        },
    getConfig().getLong("save.periodinminutes") * 20 * 60, getConfig().getLong("save.periodinminutes") * 20 * 60);
    }

    void reload(final boolean firstTime) {
        // You know, I realized that if you save a config before you load it,
        // all changes are thrown away.
        /*
         * if (!firstTime) { saveRents(); }
         */
        fullyLoaded = false;
        reloadConfig();
        plotManager = new PlotManager(this);
        plotManager.loadPlots(YamlConfiguration.loadConfiguration(plotsFile));
        plotManager.loadRents(YamlConfiguration.loadConfiguration(rentsFile));
        fullyLoaded = true;
    }

    void savePlots() {
        if (fullyLoaded)
            plotManager.savePlots(plotsFile);
    }

    void saveRents() {
        if (fullyLoaded) {
            getLogger().info("Saving rents...");
            plotManager.saveRents(rentsFile);
        }
    }
}
