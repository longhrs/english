package com.szprimary.english.core.quiz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 一次练习的验证结果。 */
public final class QuizResult {
    public final int total;
    public final int correct;
    public final int score;
    public final int stars;
    public final long durationMs;
    public final List<QuestionRecord> records;

    public QuizResult(List<QuestionRecord> records, long durationMs) {
        this.records = Collections.unmodifiableList(new ArrayList<QuestionRecord>(records));
        this.durationMs = durationMs;
        int right = 0;
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).correct) {
                right++;
            }
        }
        this.total = records.size();
        this.correct = right;
        this.score = total == 0 ? 0 : Math.round(right * 100f / total);
        this.stars = score >= 90 ? 3 : score >= 75 ? 2 : score >= 60 ? 1 : 0;
    }

    public List<QuestionRecord> wrongRecords() {
        List<QuestionRecord> wrong = new ArrayList<QuestionRecord>();
        for (int i = 0; i < records.size(); i++) {
            if (!records.get(i).correct) {
                wrong.add(records.get(i));
            }
        }
        return wrong;
    }

    public List<String> wrongWordKeys() {
        List<String> keys = new ArrayList<String>();
        for (int i = 0; i < records.size(); i++) {
            QuestionRecord r = records.get(i);
            if (!r.correct && r.question.wordKey != null && !keys.contains(r.question.wordKey)) {
                keys.add(r.question.wordKey);
            }
        }
        return keys;
    }

    public String comment() {
        if (score >= 95) {
            return "太棒了！这个单元你已经掌握得很扎实。";
        }
        if (score >= 85) {
            return "很好！错的几道题看看解析就没问题了。";
        }
        if (score >= 70) {
            return "基本过关，建议把错题再练一遍。";
        }
        if (score >= 60) {
            return "刚刚及格，回到「知识引导」把词汇和句型再读一遍吧。";
        }
        return "别着急，先学「知识引导」，再来练习会顺很多。";
    }
}
