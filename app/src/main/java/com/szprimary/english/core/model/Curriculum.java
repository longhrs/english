package com.szprimary.english.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 全部课程内容（1-6 年级）。 */
public final class Curriculum {
    public final List<Grade> grades;
    private final Map<String, Unit> unitsById = new HashMap<String, Unit>();
    private final Map<String, Word> wordsByKey = new HashMap<String, Word>();

    public Curriculum(List<Grade> grades) {
        this.grades = Collections.unmodifiableList(grades);
        for (int g = 0; g < grades.size(); g++) {
            List<Unit> units = grades.get(g).units;
            for (int u = 0; u < units.size(); u++) {
                Unit unit = units.get(u);
                unitsById.put(unit.id, unit);
                for (int w = 0; w < unit.words.size(); w++) {
                    Word word = unit.words.get(w);
                    wordsByKey.put(word.key(), word);
                }
            }
        }
    }

    public Grade grade(int number) {
        for (int i = 0; i < grades.size(); i++) {
            if (grades.get(i).grade == number) {
                return grades.get(i);
            }
        }
        return null;
    }

    public Unit unit(String id) {
        return unitsById.get(id);
    }

    public Word word(String key) {
        return wordsByKey.get(key);
    }

    public List<Unit> allUnits() {
        List<Unit> all = new ArrayList<Unit>();
        for (int i = 0; i < grades.size(); i++) {
            all.addAll(grades.get(i).units);
        }
        return all;
    }

    public List<Word> allWords() {
        List<Word> all = new ArrayList<Word>();
        List<Unit> units = allUnits();
        for (int i = 0; i < units.size(); i++) {
            all.addAll(units.get(i).words);
        }
        return all;
    }

    public int totalWords() {
        return wordsByKey.size();
    }
}
