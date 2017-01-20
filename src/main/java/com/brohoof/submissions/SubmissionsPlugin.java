package com.brohoof.submissions;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class SubmissionsPlugin extends JavaPlugin {
    
    
    private EventListener eventListener;
    private Settings settings;
    private BukkitTask task;
    private CommandHandler commandHandler;

    @Override
    public void onEnable() {
        eventListener = new EventListener(this);
        getServer().getPluginManager().registerEvents(eventListener, this);
        settings = new Settings(this);
        Plot.loadPlots(this, getConfig(settings.getPlotFileName()));
        Rent.loadRents(this, getConfig(settings.getRentFileName()));
        task = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            Plot.saveAllPlots();
            Rent.saveAllRents();
        }, settings.getSaveInterval(), settings.getSaveInterval());
        commandHandler = new CommandHandler();
    }
    
    @Override
    public void onDisable() {
        task.cancel();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return commandHandler.onCommand(sender, command, label, args);
    }
    /**
     * Gets a {@link FileConfiguration} for this plugin, read through the argument.
     * <p>
     *
     * @return Plugin configuration
     * @param name
     *            The file name to read.
     */
    public YamlConfiguration getConfig(final String name) {
        return YamlConfiguration.loadConfiguration(new File(getDataFolder(), name));
    }

    public Settings getSettings() {
        return settings;
    }
}
