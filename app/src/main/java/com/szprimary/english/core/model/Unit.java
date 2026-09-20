package com.szprimary.english.core.model;

import java.util.Collections;
import java.util.List;

/** 一个话题单元，是"知识引导"和"练习验证"的最小组织单位。 */
public final class Unit {
    public final String id;
    public final int grade;
    public final int no;
    public final String titleEn;
    public final String titleCn;
    public final String topic;
    public final String overview;
    public final Phonics phonics;
    public final List<Word> words;
    public final List<SentencePattern> patterns;
    public final List<GrammarNote> grammar;
    public final List<String> tips;

    public Unit(String id, int grade, int no, String titleEn, String titleCn, String topic, String overview,
                Phonics phonics, List<Word> words, List<SentencePattern> patterns,
                List<GrammarNote> grammar, List<String> tips) {
        this.id = id;
        this.grade = grade;
        this.no = no;
        this.titleEn = titleEn;
        this.titleCn = titleCn;
        this.topic = topic;
        this.overview = overview;
        this.phonics = phonics;
        this.words = Collections.unmodifiableList(words);
        this.patterns = Collections.unmodifiableList(patterns);
        this.grammar = Collections.unmodifiableList(grammar);
        this.tips = Collections.unmodifiableList(tips);
    }

    public String displayTitle() {
        return "Unit " + no + " " + titleEn;
    }
}
