package com.brohoof.submissions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.brohoof.submissions.exceptions.PlotCreationException;
import com.brohoof.submissions.exceptions.PlotLoadingException;

public class PlotManager {

    private final HashMap<String, Plot> plots = new HashMap<String, Plot>(0);
    private YamlConfiguration plotsConfig;
    private SubmissionsPlugin plugin;
    private World world;
    private Settings settings;

    public PlotManager(final SubmissionsPlugin plugin, final Settings settings) throws PlotLoadingException {
        this.plugin = plugin;
        plotsConfig = plugin.getConfig(settings.getPlotFileName());
        this.settings = settings;
        final Optional<World> oWorld = Optional.<World>ofNullable(Bukkit.getWorld(plotsConfig.getString("world")));
        if (!oWorld.isPresent())
            throw new PlotLoadingException("Cannot load plots, since " + plotsConfig.getString("world") + " isn't loaded.");
        world = oWorld.get();
        if (plotsConfig.getConfigurationSection("plots") == null)
            // No plots to load
            return;
        for (final String plotName : plotsConfig.getConfigurationSection("plots").getKeys(false)) {
            final ConfigurationSection plot = plotsConfig.getConfigurationSection("plots." + plotName);
            final int x1 = plot.getInt("x1");
            final int y1 = plot.getInt("y1");
            final int z1 = plot.getInt("z1");
            final int x2 = plot.getInt("x2");
            final int y2 = plot.getInt("y2");
            final int z2 = plot.getInt("z2");
            plots.put(plotName, new Plot(plotName, world, x1, y1, z1, x2, y2, z2));
        }
    }

    public Plot createPlot(final String name, final Location point1, final Location point2) throws PlotCreationException {
        if (plots.containsKey(name))
            throw new PlotCreationException("The name " + name + " is already in use.");
        final Plot plot = new Plot(name, point1, point2);
        for (final Entry<String, Plot> existing : plots.entrySet())
            for (final Location locInExisting : existing.getValue().getPlot().getCuboid(world))
                if (plot.isIn(locInExisting))
                    throw new PlotCreationException("A part of this plot is already in use by " + existing.getValue().getName());
        plots.put(name, plot);
        savePlot(plot);
        return plot;
    }

    public void deletePlot(final Plot plot) {
        try {
            plots.remove(plot.getName());
            plotsConfig.set(plot.getName(), null);
            plotsConfig.save(settings.getPlotFile());
        } catch (final IOException ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public Optional<Plot> getPlot(final Location location) {
        for (final Entry<String, Plot> existing : plots.entrySet())
            if (existing.getValue().isIn(location))
                return Optional.<Plot>of(existing.getValue());
        return Optional.<Plot>empty();
    }

    public Optional<Plot> getPlot(final String name) {
        return Optional.<Plot>ofNullable(plots.get(name));
    }

    public World getWorld() {
        return world;
    }

    public boolean isConcerned(final Player player, final World world) {
        if (player.hasPermission("submissions.admin"))
            return false;
        if (!(world == null || this.world == null) && !world.getName().equals(this.world.getName()))
            return false;
        if (!player.hasPermission("submissions.candidate"))
            return false;
        return true;
    }

    public void saveAllPlots() {
        for (final Entry<String, Plot> plot : plots.entrySet())
            savePlot(plot.getValue());
    }

    public void savePlot(final Plot plot) {
        try {
            final ConfigurationSection section = plotsConfig.createSection("plots." + plot.getName());
            final Location point1 = plot.getFirstPoint();
            final Location point2 = plot.getSecondPoint();
            section.set("x1", point1.getBlockX());
            section.set("y1", point1.getBlockY());
            section.set("z1", point1.getBlockZ());
            section.set("x2", point2.getBlockX());
            section.set("y2", point2.getBlockY());
            section.set("z2", point2.getBlockZ());
            plotsConfig.save(settings.getPlotFile());
        } catch (final IOException ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public int getTotalPlots() {
        return plots.size();
    }

    public ArrayList<Plot> getPlots() {
        final ArrayList<Plot> plots = new ArrayList<Plot>(this.plots.size());
        for (final Plot plot : this.plots.values())
            plots.add(plot);
        return plots;
    }
    
    public void dumpPlots() {
        plugin.getLogger().info("PLOT INFORMATION.");
        for(Entry<String, Plot> plot : plots.entrySet()) {
            plugin.getLogger().info("String = " + plot.getKey() + ", Plot = " + plot.getValue().toString());
        }
    }
}
