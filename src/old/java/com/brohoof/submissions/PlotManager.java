package com.brohoof.submissions;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class PlotManager {
    private int defaultExpand;
    @Deprecated
    private String dummyPrefix;
    private UUID dummyUUID;
    private String filterInPermission;
    private String filterOutPermission;
    private final Map<String, Plot> plots;
    private FileConfiguration plotsConfig;
    private final SubmissionsPlugin plugin;
    private final Map<String, Rent> rents;
    private FileConfiguration rentsConfig;
    private String worldName;

    public PlotManager(final SubmissionsPlugin plugin) {
        this.plugin = plugin;
        plots = new LinkedHashMap<String, Plot>();
        rents = new LinkedHashMap<String, Rent>();
        worldName = null;
    }

    public boolean createPlot(final String name, final Location min, final Location max) {
        if (!plots.containsKey(name)) {
            final ConfigurationSection plotsSection = plotsConfig.getConfigurationSection("plots");
            final ConfigurationSection plot = plotsSection.createSection(name);
            plot.set("outer", defaultExpand);
            plot.set("ax", min.getBlockX());
            plot.set("az", min.getBlockZ());
            plot.set("bx", max.getBlockX());
            plot.set("bz", max.getBlockZ());
            plugin.getLogger().severe("Created new plot " + name);
            plugin.savePlots();
            plugin.reload(false);
            return true;
        }
        return false;
    }

    public boolean expropriate(final String name) {
        if (plots.containsKey(name)) {
            if (rents.containsKey(name) && rents.get(name).isRented())
                return false;
            final ConfigurationSection plotsSection = plotsConfig.getConfigurationSection("plots");
            plotsSection.set(name, null);
            plugin.getLogger().severe("Expropriated plot " + name);
            plugin.savePlots();
            plugin.reload(false);
            return true;
        }
        return false;
    }

    public Collection<String> getAllPlotNames() {
        return plots.keySet();
    }

    public Collection<Rent> getAllRents() {
        return rents.values();
    }

    public Plot getBoundedPlot(final Location location) {
        for (final Plot plot : plots.values())
            if (plot.matchesBoundedLocation(location))
                return plot;
        return null;
    }

    public int getCountDummyRents() {
        int count = 0;
        for (final Rent rent : rents.values())
            if (rent.isRented() && isDummyPlayer(rent.getOwner()))
                count = count + 1;
        return count;
    }

    public int getCountOccupiedRents() {
        int count = 0;
        for (final Rent rent : rents.values())
            if (rent.isRented() && !isDummyPlayer(rent.getOwner()))
                count = count + 1;
        return count;
    }

    public Plot getExtendedPlot(final Location location) {
        for (final Plot plot : plots.values())
            if (plot.matchesExtendedLocation(location))
                return plot;
        return null;
    }

    public Plot getPlotByPlot(final String plot) {
        return plots.get(plot);
    }

    public Rent getRentByPlot(final String plot) {
        return rents.get(plot);
    }

    @Deprecated
    public Rent getRentOfPlayer(final String player) {
        for (final Rent rent : rents.values())
            if (rent.matchesOwner(player))
                return rent;
        return null;
    }

    public Rent getRentOfPlayer(final UUID player) {
        for (final Rent rent : rents.values())
            if (rent.matchesOwner(player))
                return rent;
        return null;
    }

    public int getTotalPlots() {
        return plots.size() - getCountDummyRents();
    }

    public String getWorldName() {
        return worldName;
    }

    public boolean isConcerned(final Player player, final World world) {
        if (plugin.isAdmin(player))
            return false;
        if (!matchesWorld(world))
            return false;
        if (!matchesPlayer(player))
            return false;
        return true;
    }

    @Deprecated
    public boolean isDummyPlayer(final String player) {
        return player.toLowerCase().startsWith(dummyPrefix);
    }

    public boolean isDummyPlayer(final UUID uuid) {
        return uuid.equals(dummyUUID);
    }

    public void loadPlots(final FileConfiguration section) {
        plots.clear();
        if (section == null)
            return;
        plotsConfig = section;
        worldName = section.getString("world");
        filterInPermission = section.getString("permission");
        filterOutPermission = section.getString("interdiction", null);
        defaultExpand = section.getInt("defaultexpand", 4);
        dummyPrefix = section.getString("dummyprefix", "xxxdummy");
        dummyUUID = UUID.fromString("3c879ef9-95c2-44d1-98f9-2824610477c8");
        final ConfigurationSection plotsSection = section.getConfigurationSection("plots");
        for (String key : plotsSection.getKeys(false)) {
            key = key.toLowerCase();
            final ConfigurationSection configPlot = plotsSection.getConfigurationSection(key);
            final Plot plot = new Plot(key);
            plots.put(key, plot);
            plot.load(configPlot);
        }
    }

    protected void loadRents(final FileConfiguration section) {
        rents.clear();
        if (section == null)
            return;
        rentsConfig = section;
        final ConfigurationSection rentsSection = section.getConfigurationSection("rents");
        if (rentsSection == null)
            section.createSection("rents");
        // Only load rents that have a key in plots
        for ( String key : plots.keySet()) {
            key = key.toLowerCase();
            final Rent rent = new Rent(key);
            rents.put(key, rent);
            ConfigurationSection configRent = rentsSection.getConfigurationSection(key);
            if (configRent == null)
                configRent = rentsSection.createSection(key);
            rent.load(configRent);
        }
    }

    public boolean matchesForbiddenPlayer(final Player player) {
        return filterOutPermission != null && player.hasPermission(filterOutPermission);
    }

    public boolean matchesPlayer(final Player player) {
        return player.hasPermission(filterInPermission) || filterOutPermission != null && player.hasPermission(filterOutPermission);
    }

    public boolean matchesWorld(final World world) {
        if (world == null || worldName == null)
            return false;
        return world.getName().equals(worldName);
    }

    public boolean release(final String name, final CommandSender requester) {
        final Rent rentOfPlot = getRentByPlot(name);
        if (!rentOfPlot.isRented())
            return false;
        final String owner = rentOfPlot.getOwnerName();
        rentOfPlot.releaseRent();
        plugin.saveRents();
        requester.sendMessage(plugin.getMessage("release_owned", owner, rentOfPlot.getName()));
        plugin.getLogger().info(plugin.getMessage("release_owned", owner, rentOfPlot.getName()));
        return true;
    }

    public boolean rent(final String plot, final Player player, final CommandSender requester) {
        final Rent rentOfPlot = getRentByPlot(plot);
        if (rentOfPlot.isRented())
            return false;
        if (getRentOfPlayer(player.getUniqueId()) != null)
            return false;
        rentOfPlot.rentImmediatly(player);
        plugin.saveRents();
        if (requester instanceof Player && requester.getName().toLowerCase().equals(player.getName().toLowerCase()))
            requester.sendMessage(plugin.getMessage("rented", rentOfPlot.getName()));
        else
            requester.sendMessage(plugin.getMessage("newrent", player.getName(), rentOfPlot.getName()));
        plugin.getLogger().info(plugin.getMessage("newrent", player.getName(), rentOfPlot.getName()));
        return true;
    }

    public void savePlots(final File file) {
        try {
            plotsConfig.save(file);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void saveRents(final File file) {
        try {
            rentsConfig.save(file);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
