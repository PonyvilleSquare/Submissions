package com.brohoof.submissions;

public class Simplificator {
    public static String humanTimeDiff(final long now, final long since) {
        final long sinceTotalHours = (now - since) / 3600000;
        final long sinceDays = sinceTotalHours / 24;
        final long sinceOnlyHours = sinceTotalHours % 24;
        String duration = "";
        if (sinceDays == 0 && sinceOnlyHours == 0)
            duration = "0h";
        else if (sinceDays == 0)
            duration = sinceOnlyHours + "h";
        else if (sinceOnlyHours == 0)
            duration = sinceDays + "d";
        else
            duration = sinceDays + "d " + sinceOnlyHours + "h";
        return duration;
    }
}
