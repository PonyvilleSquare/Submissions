package com.brohoof.submissions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

// From https://github.com/lihad/WhoAreYou lihad@github
public class Permission {

    public int addTry(final Player player) {
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

    public int addTry(final String name) {
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

    /**
     * 
     * @param time
     *            time in seconds
     * @return
     */
    public String getTimeRemainingUntilTime(long time) {
        time -= System.currentTimeMillis() / 1000;
        final double days = time / (double) 60 / 60 / 24;
        final double hours = (days - (int) days) * 60;
        final double mins = (hours - (int) hours) * 60;
        final int seconds = (int) ((mins - (int) mins) * 60);
        return (int) days + "d-" + (int) hours + "h-" + (int) mins + "m-" + seconds + "s";
    }

    public String getFriendlyTimeRemaning(final Player player) {
        return getFriendlyTimeRemaning(player.getName());
    }

    public String getFriendlyTimeRemaning(final String player) {
        final String time = PermissionsEx.getUser(player).getOption("group-cmcx-until");
        if (StringUtils.isEmpty(time))
            return "null";
        return getTimeRemainingUntilTime(Long.parseLong(time));

    }

    public String getGroup(final Player player) {
        return getGroup(player.getName());
    }

    public String getGroup(final String player) {
        final List<PermissionGroup> groups = PermissionsEx.getUser(player).getParents();
        return groups.size() > 0 ? PermissionsEx.getUser(player).getParents().get(0).getName() : "UNKNOWN";
    }

    public String getPrefix(final Player player) {
        return getPrefix(player.getName());
    }

    public String getPrefix(final String player) {
        return PermissionsEx.getUser(player).getPrefix();
    }

    public String getSuffix(final Player player) {
        return getSuffix(player.getName());
    }

    public String getSuffix(final String player) {
        return PermissionsEx.getUser(player).getSuffix();
    }

    /**
     * Gets a timestamp from a unix time.
     *
     * @param time
     *            in seconds
     * @return
     */
    public String getTimeStamp(long time) {
        // SYSTEM TIME IN MILISECONDS
        time *= 1000L;
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // the format of your date
        sdf.setTimeZone(TimeZone.getDefault()); // give a timezone reference for formating
        return sdf.format(new Date(time));
    }

    public String getTimeStampRemaning(final Player player) {
        return getTimeStampRemaning(player.getName());
    }

    public String getTimeStampRemaning(final String player) {
        final String time = PermissionsEx.getUser(player).getOption("group-cmcx-until");
        if (StringUtils.isEmpty(time.trim()))
            return "null";
        return getTimeStamp(Long.parseLong(time));
    }

    public int getTry(final Player player) {
        return getTry(player.getName());
    }

    public int getTry(final String player) {
        final PermissionUser permplayer = PermissionsEx.getUser(player);
        final String value = permplayer.getOption("submissions.try");
        if (StringUtils.isEmpty(value))
            return 0;
        return Integer.parseInt(value);
    }
}
