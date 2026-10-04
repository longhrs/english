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
    /** 对应课本的册次，如 "3A"；拓展内容为 "拓展"；没有对应时为空串。 */
    public final String book;
    /** 在该册课本里的单元号；没有时为 0。 */
    public final int bookUnit;

    public Unit(String id, int grade, int no, String titleEn, String titleCn, String topic, String overview,
                Phonics phonics, List<Word> words, List<SentencePattern> patterns,
                List<GrammarNote> grammar, List<String> tips) {
        this(id, grade, no, titleEn, titleCn, topic, overview, phonics, words, patterns, grammar, tips, "", 0);
    }

    public Unit(String id, int grade, int no, String titleEn, String titleCn, String topic, String overview,
                Phonics phonics, List<Word> words, List<SentencePattern> patterns,
                List<GrammarNote> grammar, List<String> tips, String book, int bookUnit) {
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
        this.book = book == null ? "" : book;
        this.bookUnit = bookUnit;
    }

    /** 标题：对应课本单元时写成「3A Unit 1 How do we feel?」，拓展内容写成「拓展 · My school」。 */
    public String displayTitle() {
        if (book.length() > 0 && bookUnit > 0) {
            return book + " Unit " + bookUnit + " " + titleEn;
        }
        if (book.length() > 0) {
            return book + " · " + titleEn;
        }
        return "Unit " + no + " " + titleEn;
    }
}
