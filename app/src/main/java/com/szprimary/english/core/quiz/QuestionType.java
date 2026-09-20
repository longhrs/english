package com.szprimary.english.core.quiz;

/** 练习题型。 */
public enum QuestionType {
    /** 看英文选中文。 */
    EN_TO_CN("英译中"),
    /** 看中文选英文。 */
    CN_TO_EN("中译英"),
    /** 看中文写单词（拼写验证）。 */
    SPELLING("单词拼写"),
    /** 句型填空，从选项中选出缺失的词。 */
    PATTERN_BLANK("句型填空"),
    /** 连词成句。 */
    SENTENCE_ORDER("连词成句");

    public final String label;

    QuestionType(String label) {
        this.label = label;
    }

    public boolean isChoice() {
        return this == EN_TO_CN || this == CN_TO_EN || this == PATTERN_BLANK;
    }
}
