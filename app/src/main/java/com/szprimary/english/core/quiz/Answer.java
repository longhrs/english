package com.szprimary.english.core.quiz;

/** 学生提交的一次作答。 */
public final class Answer {
    public final int choiceIndex;
    public final String text;

    private Answer(int choiceIndex, String text) {
        this.choiceIndex = choiceIndex;
        this.text = text;
    }

    public static Answer choice(int index) {
        return new Answer(index, null);
    }

    public static Answer text(String value) {
        return new Answer(-1, value);
    }

    public static Answer skipped() {
        return new Answer(-1, "");
    }

    public String display(Question question) {
        if (choiceIndex >= 0 && choiceIndex < question.options.size()) {
            return question.options.get(choiceIndex);
        }
        return text == null ? "" : text;
    }
}
