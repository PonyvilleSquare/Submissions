package com.brohoof.submissions;

import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.World;

public class Plot implements Comparable<Plot> {

    private final String name;
    private final WorldlessCuboid plot;
    private final Location firstPoint;
    private final Location secondPoint;

    public Plot(final String name, final Location point1, final Location point2) {
        Validate.notNull(point1, "Location cannot be null.");
        Validate.notNull(point2, "Location cannot be null.");
        Validate.notNull(name, "Name cannot be null.");
        this.name = name;
        plot = new WorldlessCuboid(point1, point2, 0);
        firstPoint = point1;
        secondPoint = point2;
    }

    public Plot(final String name, final World world, final int x1, final int y1, final int z1, final int x2, final int y2, final int z2) {
        this(name, new Location(world, x1, y1, z1), new Location(world, x2, y2, z2));
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Plot))
            return false;
        final Plot plot = (Plot) obj;
        return name.equals(plot.name);
    }
    @Override
    public int compareTo(final Plot o) {
        return name.compareTo(o.name);
    }

    public String getName() {
        return name;
    }

    public Location getFirstPoint() {
        return firstPoint;
    }

    public Location getSecondPoint() {
        return secondPoint;
    }

    public WorldlessCuboid getPlot() {
        return plot;
    }

    public boolean isIn(final Location point) {
        return plot.isIn(point);
    }

    @Override
    public String toString() {
        return "Plot [name=" + name + ", plot=" + plot + ", firstPoint=" + firstPoint + ", secondPoint=" + secondPoint + "]";
    }
}
