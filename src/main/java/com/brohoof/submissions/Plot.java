package com.brohoof.submissions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class Plot {

    private static YamlConfiguration plotsConfig;
    private static SubmissionsPlugin plugin;
    private String name;
    private WorldlessCuboid plot;
    private static final HashMap<String, Plot> plots = new HashMap<String, Plot>(0);

    public Plot(String name, WorldlessCuboid plot) {
        Validate.notNull(name, "Name cannot be null.");
        this.name = name.toLowerCase();
        this.plot = plot;
    }

    public Plot(String name, Location point1, Location point2) {
        Validate.notNull(point1, "Location cannot be null.");
        Validate.notNull(point2, "Location cannot be null.");
        Validate.notNull(name, "Name cannot be null.");
        this.name = name;
        this.plot = new WorldlessCuboid(point1, point2, 0);
    }

    public Plot(String name, int x1, int y1, int z1, int x2, int y2, int z2) {
        this(name, new Location(null, x1, y1, z1), new Location(null, x2, y2, z2));
    }

    public String getName() {
        return name;
    }

    public boolean isIn(Location point) {
        return plot.isIn(point);
    }
    public WorldlessCuboid getPlot() {
        return plot;
    }

    public Location getPoint1() {
        return new Location(null, plot.xMin, plot.yMin, plot.zMin);
    }

    public Location getPoint2() {
        return new Location(null, plot.xMax, plot.yMax, plot.zMax);
    }

    public static void loadPlots(SubmissionsPlugin plugin, YamlConfiguration plots) {
        Plot.plugin = plugin;
        plotsConfig = plots;
        for (String plotName : plots.getKeys(false)) {
            ConfigurationSection plot = plots.getConfigurationSection(plotName);
            int x1 = plot.getInt("x1");
            int y1 = plot.getInt("y1");
            int z1 = plot.getInt("z1");
            int x2 = plot.getInt("x2");
            int y2 = plot.getInt("y2");
            int z2 = plot.getInt("z2");
            Plot.plots.put(plotName, new Plot(plotName, x1, y1, z1, x2, y2, z2));
        }
    }

    public static Optional<Plot> getPlot(String name) {
        return Optional.<Plot>ofNullable(plots.get(name));
    }

    public static Plot createPlot(String name, Location point1, Location point2) throws PlotCreationException {
        if (plots.containsKey(name))
            throw new PlotCreationException("The name " + name + " is already in use.");
        Plot plot = new Plot(name, point1, point2);
        savePlot(plot);
        return plot;
    }

    public static void savePlot(Plot plot) {
        try {
            ConfigurationSection section = plotsConfig.createSection(plot.getName());
            section.set("x1", plot.plot.xMin);
            section.set("y1", plot.plot.yMin);
            section.set("z1", plot.plot.zMin);
            section.set("x2", plot.plot.xMax);
            section.set("y2", plot.plot.zMax);
            section.set("z2", plot.plot.yMax);
            plotsConfig.save(plugin.getSettings().getPlotFile());
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
        }
    }
    
    public static void deletePlot(Plot plot) {
        try {
            Plot.plots.remove(plot.name);
            plotsConfig.set(plot.name, null);
            plotsConfig.save(plugin.getSettings().getPlotFile());
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
        }
    }
    
    public static void saveAllPlots() {
        for(Entry<String, Plot> plot : Plot.plots.entrySet())
            savePlot(plot.getValue());
    }
}
