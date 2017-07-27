package com.brohoof.submissions;

import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Rent implements Comparable<Rent> {
    private int changes;
    private final long created;
    private long modified;
    private final UUID owner;
    private String ownerName;
    private final Plot plot;

    public Rent(final UUID owner, final String ownerName, final Plot plot, final int changes, final long created, final long modified) {
        this.owner = owner;
        this.ownerName = ownerName;
        this.plot = plot;
        this.changes = changes;
        this.created = created;
        this.modified = modified;
    }

    @Override
    public String toString() {
        return "Rent [changes=" + changes + ", created=" + created + ", modified=" + modified + ", owner=" + owner + ", ownerName=" + ownerName + ", plot=" + plot + "]";
    }

    @Override
    public int compareTo(final Rent o) {
        return Long.compare(created, o.created);
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

    public void setOwnerName(final String name) {
        Validate.notEmpty(name);
        ownerName = name;

    }

}
