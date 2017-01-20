package com.brohoof.submissions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

// From https://github.com/lihad/WhoAreYou lihad@github

import org.bukkit.entity.Player;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

class Permission {
    private SubmissionsPlugin p;

    Permission(final SubmissionsPlugin instance) {
        p = instance;
    }

    int addTry(final Player player, final String name) {
        if (player != null) {
            final PermissionUser permplayer = PermissionsEx.getUser(player);
            final String value = permplayer.getOption("submissions.try");
            if (StringUtils.isEmpty(value)) {
                permplayer.setOption("submissions.try", "1");
                return 1;
            }
            final int oldtries = Integer.parseInt(value);
            final int newtries = oldtries + 1;
            permplayer.setOption("submissions.try", String.valueOf(newtries));
            return newtries;
        }
        final PermissionUser permplayer = PermissionsEx.getUser(name);
        final String value = permplayer.getOption("submissions.try");
        if (StringUtils.isEmpty(value)) {
            permplayer.setOption("submissions.try", "1");
            return 1;
        }
        final int oldtries = Integer.parseInt(value);
        final int newtries = oldtries + 1;
        permplayer.setOption("submissions.try", String.valueOf(newtries));
        return newtries;
    }

    String getGroup(final Player player) {
        return getGroup(player.getName());
    }

    String getTimeStampRemaning(final Player player) {
        try {
            String time = PermissionsEx.getUser(player).getOption("group-cmcx-until");
            if (time == null || time == "")
                return "null";
            return getTimeStamp(Long.parseLong(time));
        } catch (Exception e) {
            p.getLogger().info("This error occured on 59.");
            e.printStackTrace();
            return null;
        }
    }

    String getTimeStampRemaning(final String player) {
        try {
            String time = PermissionsEx.getUser(player).getOption("group-cmcx-until");
            if (time == null || time == "")
                return "null";
            return getTimeStamp(Long.parseLong(time));
        } catch (Exception e) {
            p.getLogger().info("This error occured on 56.");
            e.printStackTrace();
            return null;
        }
    }

    String getFriendlyTimeRemaning(final Player player) {
        try {
            String time = PermissionsEx.getUser(player).getOption("group-cmcx-until");
            if (time == null || time == "")
                return "null";
            return getExpires(Long.parseLong(time));
        } catch (Exception e) {
            p.getLogger().info("This error occured on 79.");
            e.printStackTrace();
            return null;
        }
    }

    String getFriendlyTimeRemaning(final String player) {
        try {
            String time = PermissionsEx.getUser(player).getOption("group-cmcx-until");
            if (time == null || time == "")
                return "null";
            return getExpires(Long.parseLong(time));
        } catch (Exception e) {
            p.getLogger().info("This error occured on 89.");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 
     * @param time time in seconds
     * @return
     */
    private static String getExpires(long time) {
        time -= System.currentTimeMillis() / 1000;
        double days = time / (double) 60 / 60 / 24;
        double hours = (days - (int) days) * 60;
        double mins = (hours - (int) hours) * 60;
        int seconds = (int) ((mins - (int) mins) * 60);
        return (int) days + "d-" + (int) hours + "h-" + (int) mins + "m-" + seconds + "s";
    }

    /**
     * Gets a timestamp from a unix time.
     *
     * @param time in seconds
     * @return
     */
    protected String getTimeStamp(long time) {
        // SYSTEM TIME IN MILISECONDS
        time *= 1000L;
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // the format of your date
        sdf.setTimeZone(TimeZone.getDefault()); // give a timezone reference for formating
        return sdf.format(new Date(time));
    }

    String getGroup(final String player) {
        final List<PermissionGroup> groups = PermissionsEx.getUser(player).getParents();
        // Just in case: PermissionEx doc doesn't tell in which order these are.
        /*
         * if (groups.length != 1 && !this.warnings.contains(player)) { this.warnings.add(player); String groupMode = ""; for (int i = 0; i < groups.length; i++) { groupMode = groupMode + groups[i].getName() + "(" + i + ")"; if (i != groups.length - 1) { groupMode = groupMode + " "; } } this.plugin.getLogger().info( "Player " + player.getName() + " is in " + groups.length + " groups! (" + groupMode + ")"); }
         */
        return groups.size() > 0 ? PermissionsEx.getUser(player).getParents().get(0).getName() : "UNKNOWN";
    }

    String getPrefix(final Player player) {
        return getPrefix(player.getName());
    }

    String getPrefix(final String player) {
        return PermissionsEx.getUser(player).getPrefix();
    }

    String getSuffix(final Player player) {
        return getSuffix(player.getName());
    }

    String getSuffix(final String player) {
        return PermissionsEx.getUser(player).getSuffix();
    }

    int getTry(final Player player, final String playername) {
        if (player != null) {
            final PermissionUser permplayer = PermissionsEx.getUser(player);
            final String value = permplayer.getOption("submissions.try");
            if (StringUtils.isEmpty(value))
                return 0;
            return Integer.parseInt(value);
        }
        final PermissionUser permplayer = PermissionsEx.getUser(playername);
        final String value = permplayer.getOption("submissions.try");
        if (StringUtils.isEmpty(value))
            return 0;
        return Integer.parseInt(value);
    }
}
