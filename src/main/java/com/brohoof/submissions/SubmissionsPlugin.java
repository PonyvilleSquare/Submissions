package com.brohoof.submissions;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class SubmissionsPlugin extends JavaPlugin {

    private CommandHandler commandHandler;
    private EventListener eventListener;
    private PlotManager plotManager;
    private RentManager rentManager;
    private Settings settings;

    private BukkitTask task;
    private Permission permission;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        settings = new Settings(this);
        try {
            plotManager = new PlotManager(this, settings);
            rentManager = new RentManager(this, settings, plotManager);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
        permission = new Permission();
        eventListener = new EventListener(this, permission, plotManager, rentManager);
        commandHandler = new CommandHandler(plotManager, rentManager, permission);
        task = getServer().getScheduler().runTaskTimer(this, () -> {
            plotManager.saveAllPlots();
            rentManager.saveAllRents();
        }, settings.getSaveInterval(), settings.getSaveInterval());
        getServer().getPluginManager().registerEvents(eventListener, this);
    }

    @Override
    public void onDisable() {
        task.cancel();
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        return commandHandler.onCommand(sender, command, label, args);
    }

    public YamlConfiguration getConfig(final String name) {
        return YamlConfiguration.loadConfiguration(new File(getDataFolder(), name));
    }

    public static WorldEditPlugin getWorldEdit() {
        return JavaPlugin.getPlugin(WorldEditPlugin.class);
    }
}
