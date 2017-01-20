package com.brohoof.submissions;

import java.util.Comparator;

public class CreatedComparator implements Comparator<Rent> {
    public CreatedComparator() {
    }

    @Override
    public int compare(final Rent o1, final Rent o2) {
        if (o1.getCreated() < o2.getCreated())
            return -1;
        if (o1.getCreated() == o2.getCreated())
            return 0;
        return 1;
    }
}
