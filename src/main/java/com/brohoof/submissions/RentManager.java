package com.brohoof.submissions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.brohoof.submissions.exceptions.RentLoadingException;
import com.google.common.collect.HashBiMap;

public class RentManager {
    private final HashMap<String, UUID> playerNames = new HashMap<String, UUID>(0);
    private SubmissionsPlugin plugin;
    private final HashBiMap<UUID, Rent> rents = HashBiMap.<UUID, Rent>create(0);
    private YamlConfiguration rentsConfig;
    private Settings settings;

    public RentManager(final SubmissionsPlugin plugin, final Settings settings, final PlotManager plotManager) throws RentLoadingException {
        this.plugin = plugin;
        rentsConfig = plugin.getConfig(settings.getRentFileName());
        this.settings = settings;
        if (rentsConfig.getConfigurationSection("rents") == null)
            // No plots to load
            return;
        for (final String rentName : rentsConfig.getConfigurationSection("rents").getKeys(false)) {
            final ConfigurationSection rent = rentsConfig.getConfigurationSection("rents." + rentName);
            final UUID owner = UUID.fromString(rent.getString("owner"));
            final String ownerName = rent.getString("ownerName");
            final int changes = rent.getInt("changes");
            final long created = rent.getLong("created");
            final long modified = rent.getLong("modified");
            final Optional<Plot> pPlot = plotManager.getPlot(rentName);
            if (!pPlot.isPresent())
                throw new RentLoadingException("Attempted to load a rent whose plot does not exist!");
            rents.put(owner, new Rent(owner, ownerName, pPlot.get(), changes, created, modified));
            playerNames.put(ownerName, owner);
        }
    }

    public Rent createRent(final Plot plot, final Player renter) {
        final Rent rent = new Rent(renter.getUniqueId(), renter.getName(), plot, 0, System.currentTimeMillis(), System.currentTimeMillis());
        rents.put(renter.getUniqueId(), rent);
        playerNames.put(rent.getOwnerName(), rent.getOwner());
        saveRent(rent);
        return rent;
    }

    public Optional<Rent> getRent(final Location loc) {
        for (final Entry<UUID, Rent> rent : rents.entrySet())
            if (rent.getValue().getPlot().isIn(loc))
                return Optional.<Rent>of(rent.getValue());
        return Optional.<Rent>empty();
    }

    public Optional<Rent> getRent(final Plot plot) {
        for (final Entry<UUID, Rent> rent : rents.entrySet())
            if (rent.getValue().getPlot().equals(plot))
                return Optional.<Rent>of(rent.getValue());
        return Optional.<Rent>empty();
    }

    public Optional<Rent> getRent(final String ownerName) {
        return Optional.<Rent>ofNullable(rents.get(playerNames.get(ownerName)));
    }

    public Optional<Rent> getRent(final UUID owner) {
        return Optional.<Rent>ofNullable(rents.get(owner));
    }

    public ArrayList<Rent> getRents() {
        final ArrayList<Rent> rents = new ArrayList<Rent>(this.rents.size());
        for (final Rent rent : this.rents.values())
            rents.add(rent);
        return rents;
    }

    public void removeRent(final Rent rent) {
        try {
            rents.remove(rent.getOwner());
            rentsConfig.set("rents." + rent.getPlot().getName(), null);
            rentsConfig.save(settings.getRentFile());
        } catch (final IOException ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public void saveAllRents() {
        for (final Entry<UUID, Rent> rent : rents.entrySet())
            saveRent(rent.getValue());
    }

    public void saveRent(final Rent rent) {
        try {
            ConfigurationSection rentCnf = rentsConfig.getConfigurationSection("rents." + rent.getPlot().getName());
            if (rentCnf == null) {
                rentsConfig.createSection("rents." + rent.getPlot().getName());
                rentCnf = rentsConfig.getConfigurationSection("rents." + rent.getPlot().getName());
            }
            rentCnf.set("owner", rent.getOwner().toString());
            rentCnf.set("ownerName", rent.getOwnerName());
            rentCnf.set("changes", rent.getChanges());
            rentCnf.set("created", rent.getCreated());
            rentCnf.set("modified", rent.getModified());
            rentsConfig.save(settings.getRentFile());
        } catch (final IOException ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public int getTotalRents() {
        return rents.size();
    }
}
