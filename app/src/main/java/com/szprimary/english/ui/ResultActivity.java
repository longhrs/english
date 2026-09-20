package com.szprimary.english.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.szprimary.english.core.quiz.QuestionRecord;
import com.szprimary.english.core.quiz.QuizResult;

import java.util.List;

/** 练习报告：得分、星级、错题回顾与后续建议。 */
public final class ResultActivity extends Activity {

    private AppRepo repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repo = AppRepo.get(this);
        getWindow().setStatusBarColor(Ui.PRIMARY_DARK);
        build();
    }

    private void build() {
        final QuizResult result = repo.lastResult;
        LinearLayout content = Ui.page(this, "练习报告", null);
        if (result == null) {
            content.addView(Ui.body(this, "没有可显示的练习结果。"));
            return;
        }

        LinearLayout score = Ui.card(this);
        score.setGravity(Gravity.CENTER_HORIZONTAL);
        score.addView(Ui.text(this, repo.lastQuizTitle == null ? "练习" : repo.lastQuizTitle, 14, Ui.SUB, false));
        score.addView(Ui.spacer(this, 6));
        score.addView(Ui.text(this, String.valueOf(result.score), 52,
                result.score >= 60 ? Ui.PRIMARY : Ui.RED, true));
        score.addView(Ui.text(this, "分", 14, Ui.SUB, false));
        score.addView(Ui.spacer(this, 6));
        score.addView(Ui.text(this, Ui.stars(result.stars), 26, Ui.GOLD, false));
        score.addView(Ui.spacer(this, 8));
        score.addView(Ui.text(this, "答对 " + result.correct + " / " + result.total
                + " 题 · 用时 " + formatDuration(result.durationMs), 14, Ui.SUB, false));
        score.addView(Ui.spacer(this, 8));
        score.addView(Ui.text(this, result.comment(), 15, Ui.TEXT, false));
        content.addView(score);

        List<QuestionRecord> wrong = result.wrongRecords();
        if (wrong.isEmpty()) {
            LinearLayout perfect = Ui.card(this);
            perfect.addView(Ui.text(this, "全部答对，没有错题！", 16, Ui.GREEN, true));
            perfect.addView(Ui.spacer(this, 4));
            perfect.addView(Ui.hint(this, "这些词会进入间隔复习队列，过几天再巩固一次效果最好。"));
            content.addView(perfect);
        } else {
            content.addView(Ui.text(this, "错题回顾（" + wrong.size() + " 题）", 17, Ui.TEXT, true));
            content.addView(Ui.spacer(this, 8));
            for (int i = 0; i < wrong.size(); i++) {
                QuestionRecord record = wrong.get(i);
                LinearLayout card = Ui.card(this);
                card.addView(Ui.text(this, (i + 1) + ". " + record.question.prompt, 16, Ui.TEXT, true));
                card.addView(Ui.spacer(this, 6));
                String mine = record.answer == null ? "" : record.answer.display(record.question);
                card.addView(Ui.text(this, "你的答案：" + (mine.trim().length() == 0 ? "未作答" : mine), 14, Ui.RED, false));
                card.addView(Ui.text(this, "正确答案：" + record.question.correctText, 14, Ui.GREEN, true));
                card.addView(Ui.divider(this));
                card.addView(Ui.body(this, record.question.explanation));
                content.addView(card);
            }
        }

        final String unitId = repo.lastQuizUnitId;
        Button again = Ui.primary(this, "再练一遍");
        again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultActivity.this, QuizActivity.class);
                if (unitId != null) {
                    intent.putExtra("mode", "unit");
                    intent.putExtra("unitId", unitId);
                } else {
                    intent.putExtra("mode", "review");
                }
                startActivity(intent);
                finish();
            }
        });
        content.addView(again);

        if (!repo.progress().wrongBook().isEmpty()) {
            Button wrongBook = Ui.soft(this, "只练错题（" + repo.progress().wrongBook().size() + " 个词）");
            wrongBook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ResultActivity.this, QuizActivity.class);
                    intent.putExtra("mode", "wrong");
                    startActivity(intent);
                    finish();
                }
            });
            content.addView(wrongBook);
        }

        Button back = Ui.soft(this, "返回");
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        content.addView(back);
    }

    private static String formatDuration(long ms) {
        long seconds = Math.max(0, ms / 1000);
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return minutes > 0 ? (minutes + " 分 " + seconds + " 秒") : (seconds + " 秒");
    }
}
