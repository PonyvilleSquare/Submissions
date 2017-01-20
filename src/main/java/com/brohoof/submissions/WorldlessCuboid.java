package com.brohoof.submissions;

import org.bukkit.Location;
import org.bukkit.World;

/*
 * Courtesy of http://forums.bukkit.org/threads/region-general-api-for-creating-cuboids.34644/#post-638450
 */
public class WorldlessCuboid {
    public int xMin, xMax, yMin, yMax, zMin, zMax;

    public WorldlessCuboid(final Location point1, final Location point2, final int expand) {
        xMin = Math.min(point1.getBlockX(), point2.getBlockX()) - expand;
        xMax = Math.max(point1.getBlockX(), point2.getBlockX()) + expand;
        yMin = Math.min(point1.getBlockY(), point2.getBlockY());
        yMax = Math.max(point1.getBlockY(), point2.getBlockY());
        zMin = Math.min(point1.getBlockZ(), point2.getBlockZ()) - expand;
        zMax = Math.max(point1.getBlockZ(), point2.getBlockZ()) + expand;
    }

    public Location createMax(final World world) {
        return new Location(world, xMax, yMax, zMax);
    }

    public Location createMin(final World world) {
        return new Location(world, xMin, yMin, zMin);
    }

    public String getAnchor() {
        return xMin + "," + yMin + "," + zMin;
    }

    public String getSize() {
        return xMax - xMin + 1 + "x" + (yMax - yMin + 1) + "x" + (zMax - zMin + 1);
    }

    public boolean isIn(final Location loc) {
        if (loc.getBlockX() < xMin)
            return false;
        if (loc.getBlockX() > xMax)
            return false;
        if (loc.getBlockY() < yMin)
            return false;
        if (loc.getBlockY() > yMax)
            return false;
        if (loc.getBlockZ() < zMin)
            return false;
        if (loc.getBlockZ() > zMax)
            return false;
        return true;
    }
}