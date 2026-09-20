package com.szprimary.english.core.quiz;

import java.util.Locale;

/** 答案判定：大小写、首尾空格、多余空格和句末标点都不计较，其余必须一致。 */
public final class Grader {

    private Grader() {
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim().toLowerCase(Locale.ENGLISH);
        s = s.replace('’', '\'').replace('‘', '\'');
        s = s.replace('“', '"').replace('”', '"');
        s = s.replaceAll("[\\s ]+", " ");
        s = s.replaceAll("[.!?,;:]+$", "");
        return s.trim();
    }

    public static boolean isCorrect(Question question, Answer answer) {
        if (answer == null) {
            return false;
        }
        if (question.type.isChoice()) {
            return answer.choiceIndex >= 0 && answer.choiceIndex == question.correctIndex;
        }
        return normalize(answer.text).equals(normalize(question.correctText));
    }
}
