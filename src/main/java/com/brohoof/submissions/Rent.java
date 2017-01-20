package com.brohoof.submissions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class Rent {
    private final static HashMap<UUID, Rent> rents = new HashMap<UUID, Rent>(0);
    private final static HashMap<String, UUID> playerNames = new HashMap<String, UUID>(0);
    private static YamlConfiguration rentsConfig;
    private static SubmissionsPlugin plugin;
    private int changes;
    private long created;
    private long modified;
    private UUID owner;
    private String ownerName;
    private Plot plot;

    public Rent(UUID owner, String ownerName, Plot plot, int changes, long created, long modified) {
        this.owner = owner;
        this.ownerName = ownerName;
        this.plot = plot;
        this.changes = changes;
        this.created = created;
        this.modified = modified;
    }

    public int getChanges() {
        return changes;
    }

    public long getCreated() {
        return created;
    }

    public long getModified() {
        return modified;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getOwnerName() {
        return ownerName;
    }

    @SuppressWarnings("deprecation")
    public Optional<Player> getPlayer() {
        Player p = null;
        p = Bukkit.getPlayer(owner);
        if (p == null)
            p = Bukkit.getPlayer(ownerName);
        return Optional.<Player>ofNullable(p);
    }

    public Plot getPlot() {
        return plot;
    }

    public void incrementChanges() {
        changes++;
        modified = System.currentTimeMillis();
    }

    public static void loadRents(SubmissionsPlugin plugin, YamlConfiguration rents) {
        Rent.plugin = plugin;
        rentsConfig = rents;
        for (String rentName : rents.getKeys(false)) {
            ConfigurationSection rent = rents.getConfigurationSection(rentName);
            UUID owner = UUID.fromString(rent.getString("owner"));
            String ownerName = rent.getString("ownerName");
            int changes = rent.getInt("changes");
            long created = rent.getLong("created");
            long modified = rent.getLong("modified");
            Optional<Plot> pPlot = Plot.getPlot(rentName);
            if (!pPlot.isPresent())
                throw new IllegalStateException("Attempted to load a rent whose plot does not exist!");
            Rent.rents.put(owner, new Rent(owner, ownerName, pPlot.get(), changes, created, modified));
            Rent.playerNames.put(ownerName, owner);
        }
    }

    public static Rent createRent(Plot plot, Player renter) {
        Rent rent = new Rent(renter.getUniqueId(), renter.getName(), plot, 0, System.currentTimeMillis(), System.currentTimeMillis());
        Rent.rents.put(renter.getUniqueId(), rent);
        Rent.playerNames.put(rent.ownerName, rent.owner);
        return null;
    }

    public static Optional<Rent> getRent(UUID owner) {
        return Optional.<Rent>ofNullable(rents.get(owner));
    }

    public static Optional<Rent> getRent(String ownerName) {
        return Optional.<Rent>ofNullable(rents.get(playerNames.get(ownerName)));
    }

    public static void saveRent(Rent rent) {
        try {
            ConfigurationSection rentCnf = rentsConfig.getConfigurationSection(rent.plot.getName());
            rentCnf.set("owner", rent.owner.toString());
            rentCnf.set("ownerName", rent.ownerName);
            rentCnf.set("changes", rent.changes);
            rentCnf.set("created", rent.created);
            rentCnf.set("modified", rent.modified);
            rentsConfig.save(plugin.getSettings().getRentFile());
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public static void removeRent(Rent rent) {
        try {
            Rent.rents.remove(rent.ownerName);
            rentsConfig.set(rent.plot.getName(), null);
            rentsConfig.save(plugin.getSettings().getRentFile());
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
        }
    }
    
    public static void saveAllRents() {
        for(Entry<UUID, Rent> rent : Rent.rents.entrySet())
            saveRent(rent.getValue());
    }
}
