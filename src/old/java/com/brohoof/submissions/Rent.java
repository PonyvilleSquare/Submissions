package com.brohoof.submissions;

import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class Rent {
    private ConfigurationSection configRent;
    private final String name;

    public Rent(final String name) {
        this.name = name;
    }

    public int getChanges() {
        return configRent.getInt("changes", 0);
    }

    public long getCreated() {
        return configRent.getLong("created", 0);
    }

    public long getModified() {
        return configRent.getLong("modified", 0);
    }

    public String getName() {
        return name;
    }

    public UUID getOwner() {
        return UUID.fromString(configRent.getString("uuid"));
    }

    @Deprecated
    public String getOwnerName() {
        return configRent.getString("owner");
    }

    public void incrementChanges() {
        configRent.set("changes", configRent.getInt("changes", 0) + 1);
        configRent.set("modified", System.currentTimeMillis());
    }

    public boolean isRented() {
        return !configRent.getString("owner", "").equals("");
    }

    public void load(final ConfigurationSection configRent) {
        this.configRent = configRent;
    }

    @Deprecated
    public boolean matchesOwner(final String player) {
        return configRent.getString("owner", "").equalsIgnoreCase(player);
    }

    public boolean matchesOwner(final UUID player) {
        return UUID.fromString(configRent.getString("uuid", "")).equals(player);
    }

    public void releaseRent() {
        configRent.set("owner", "");
        configRent.set("uuid", "3c879ef9-95c2-44d1-98f9-2824610477c8");
        configRent.set("created", System.currentTimeMillis());
        configRent.set("modified", System.currentTimeMillis());
        configRent.set("changes", 0);
    }

    public void rentImmediatly(final Player player) {
        configRent.set("owner", player.getName());
        configRent.set("uuid", player.getUniqueId().toString());
        configRent.set("created", System.currentTimeMillis());
        configRent.set("modified", System.currentTimeMillis());
        configRent.set("changes", 0);
    }

    public void setOwnerUUID(final UUID uuid) {
        configRent.set("uuid", uuid.toString());
    }

    public void setOwnerName(String name) {
        configRent.set("owner", name);
    }
}
