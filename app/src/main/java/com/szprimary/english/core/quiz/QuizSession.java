package com.szprimary.english.core.quiz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 练习过程的状态机：出题 → 作答 → 立即判定 → 下一题 → 出报告。 */
public final class QuizSession {
    private final List<Question> questions;
    private final List<QuestionRecord> records = new ArrayList<QuestionRecord>();
    private final long startedAt;
    private int index;
    private long questionStartedAt;
    private QuestionRecord lastRecord;

    public QuizSession(List<Question> questions, long startedAt) {
        this.questions = Collections.unmodifiableList(new ArrayList<Question>(questions));
        this.startedAt = startedAt;
        this.questionStartedAt = startedAt;
    }

    public boolean isEmpty() {
        return questions.isEmpty();
    }

    public int total() {
        return questions.size();
    }

    public int position() {
        return index;
    }

    public int answered() {
        return records.size();
    }

    public int correctCount() {
        int n = 0;
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).correct) {
                n++;
            }
        }
        return n;
    }

    public Question current() {
        return index < questions.size() ? questions.get(index) : null;
    }

    public QuestionRecord lastRecord() {
        return lastRecord;
    }

    /** 提交当前题的答案，立即返回是否正确。重复提交同一题会被忽略。 */
    public boolean submit(Answer answer, long now) {
        Question question = current();
        if (question == null || records.size() > index) {
            return lastRecord != null && lastRecord.correct;
        }
        boolean correct = Grader.isCorrect(question, answer);
        lastRecord = new QuestionRecord(question, answer, correct, now - questionStartedAt);
        records.add(lastRecord);
        return correct;
    }

    /** 进入下一题，返回是否还有题目。 */
    public boolean next(long now) {
        if (index < questions.size()) {
            index++;
            questionStartedAt = now;
        }
        return index < questions.size();
    }

    public boolean finished() {
        return records.size() >= questions.size();
    }

    public QuizResult result(long now) {
        return new QuizResult(records, now - startedAt);
    }
}
