package com.szprimary.english.core.quiz;

/** 一道题的作答记录。 */
public final class QuestionRecord {
    public final Question question;
    public final Answer answer;
    public final boolean correct;
    public final long elapsedMs;

    public QuestionRecord(Question question, Answer answer, boolean correct, long elapsedMs) {
        this.question = question;
        this.answer = answer;
        this.correct = correct;
        this.elapsedMs = elapsedMs;
    }
}
