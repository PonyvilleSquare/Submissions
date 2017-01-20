package com.brohoof.submissions;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class Plot {
    private boolean infiniteY;
    private WorldlessCuboid innerCuboid;
    private final String name;
    private WorldlessCuboid outerCuboid;

    public Plot(final String name) {
        this.name = name;
    }

    public String getAnchor() {
        return innerCuboid.getAnchor();
    }

    public WorldlessCuboid getBounded() {
        return innerCuboid;
    }

    public WorldlessCuboid getExtended() {
        return outerCuboid;
    }

    public String getName() {
        return name;
    }

    public String getSize() {
        return innerCuboid.getSize();
    }

    public void load(final ConfigurationSection configPlot) {
        infiniteY = configPlot.getBoolean("infinitey", true);
        final int outer = configPlot.getInt("outer", 0);
        final double ax = configPlot.getInt("ax");
        final double ay = !infiniteY ? configPlot.getInt("ay") : 0;
        final double az = configPlot.getInt("az");
        final double bx = configPlot.getInt("bx");
        final double by = !infiniteY ? configPlot.getInt("by") : 255;
        final double bz = configPlot.getInt("bz");
        innerCuboid = new WorldlessCuboid(new Location(null, ax, ay, az), new Location(null, bx, by, bz), 0);
        outerCuboid = new WorldlessCuboid(new Location(null, ax, ay, az), new Location(null, bx, by, bz), outer);
    }

    public boolean matchesBoundedLocation(final Location location) {
        return innerCuboid.isIn(location);
    }

    public boolean matchesExtendedLocation(final Location location) {
        return outerCuboid.isIn(location);
    }
}
