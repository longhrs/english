package com.szprimary.english.core.quiz;

/** 出题参数。seed 固定时出题结果可复现，便于测试。 */
public final class QuizOptions {
    public int count = 10;
    public long seed = System.currentTimeMillis();
    public boolean allowSpelling = true;
    public boolean allowPatterns = true;

    public static QuizOptions of(int count, long seed) {
        QuizOptions o = new QuizOptions();
        o.count = count;
        o.seed = seed;
        return o;
    }

    /** 一二年级以听说认读为主，不考默写拼写。 */
    public QuizOptions forGrade(int grade) {
        this.allowSpelling = grade >= 3;
        return this;
    }
}
