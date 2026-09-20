package com.szprimary.english.core.quiz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 一道练习题。选择题用 options/correctIndex，拼写和连词成句用 correctText。 */
public final class Question {
    public final QuestionType type;
    public final String prompt;
    public final String hint;
    public final List<String> options;
    public final int correctIndex;
    public final String correctText;
    public final String explanation;
    public final String speakText;
    public final String wordKey;
    public final String unitId;

    public Question(QuestionType type, String prompt, String hint, List<String> options, int correctIndex,
                    String correctText, String explanation, String speakText, String wordKey, String unitId) {
        this.type = type;
        this.prompt = prompt;
        this.hint = hint;
        this.options = Collections.unmodifiableList(options == null ? new ArrayList<String>() : options);
        this.correctIndex = correctIndex;
        this.correctText = correctText;
        this.explanation = explanation;
        this.speakText = speakText;
        this.wordKey = wordKey;
        this.unitId = unitId;
    }

    /** 连词成句时打乱后的词块；其它题型返回空表。 */
    public List<String> tokens() {
        List<String> tokens = new ArrayList<String>();
        if (type == QuestionType.SENTENCE_ORDER) {
            tokens.addAll(options);
        }
        return tokens;
    }
}
