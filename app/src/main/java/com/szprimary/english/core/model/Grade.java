package com.szprimary.english.core.model;

import java.util.Collections;
import java.util.List;

/** 一个年级，含若干话题单元。 */
public final class Grade {
    public final int grade;
    public final String title;
    public final String subtitle;
    public final List<Unit> units;

    public Grade(int grade, String title, String subtitle, List<Unit> units) {
        this.grade = grade;
        this.title = title;
        this.subtitle = subtitle;
        this.units = Collections.unmodifiableList(units);
    }

    public int wordCount() {
        int n = 0;
        for (int i = 0; i < units.size(); i++) {
            n += units.get(i).words.size();
        }
        return n;
    }
}
