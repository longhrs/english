package com.szprimary.english.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.szprimary.english.core.model.Grade;
import com.szprimary.english.core.model.Unit;
import com.szprimary.english.core.progress.ProgressManager;
import com.szprimary.english.core.progress.UnitProgress;

import java.util.List;

/** 学习统计：总体数据 + 各年级、各单元进度。 */
public final class StatsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Ui.PRIMARY_DARK);
        build();
    }

    private void build() {
        AppRepo repo = AppRepo.get(this);
        ProgressManager progress = repo.progress();
        long now = System.currentTimeMillis();
        LinearLayout content = Ui.page(this, "学习统计", null);

        LinearLayout summary = Ui.card(this);
        summary.addView(Ui.text(this, "总体情况", 17, Ui.TEXT, true));
        summary.addView(Ui.spacer(this, 10));
        LinearLayout row1 = Ui.row(this);
        row1.addView(block(String.valueOf(progress.studyDayCount()), "学习天数"));
        row1.addView(block(String.valueOf(progress.streakDays(now)), "连续天数"));
        row1.addView(block(String.valueOf(progress.totalAnswered()), "累计答题"));
        summary.addView(row1);
        summary.addView(Ui.spacer(this, 12));
        LinearLayout row2 = Ui.row(this);
        row2.addView(block(progress.accuracyPercent() + "%", "正确率"));
        row2.addView(block(String.valueOf(progress.learnedWordCount()), "已学词"));
        row2.addView(block(String.valueOf(progress.masteredWordCount()), "已掌握"));
        summary.addView(row2);
        content.addView(summary);

        List<Grade> grades = repo.curriculum().grades;
        for (int i = 0; i < grades.size(); i++) {
            Grade grade = grades.get(i);
            LinearLayout card = Ui.card(this);
            LinearLayout head = Ui.row(this);
            head.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout titles = Ui.column(this);
            titles.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            titles.addView(Ui.text(this, grade.title, 16, Ui.TEXT, true));
            titles.addView(Ui.text(this, grade.wordCount() + " 个词汇", 12, Ui.SUB, false));
            head.addView(titles);
            int percent = repo.gradeLearnedPercent(grade);
            head.addView(Ui.text(this, percent + "%", 18, percent > 0 ? Ui.PRIMARY : Ui.SUB, true));
            card.addView(head);
            card.addView(Ui.progress(this, percent));

            for (int j = 0; j < grade.units.size(); j++) {
                Unit unit = grade.units.get(j);
                UnitProgress up = progress.unit(unit.id);
                LinearLayout line = Ui.row(this);
                line.setPadding(0, Ui.dp(this, 6), 0, 0);
                LinearLayout label = Ui.column(this);
                label.setLayoutParams(new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                label.addView(Ui.text(this, unit.displayTitle() + " · " + unit.titleCn, 13, Ui.SUB, false));
                line.addView(label);
                String right = up.attempts == 0 ? "未练习"
                        : (up.bestScore + " 分 " + Ui.stars(up.stars()));
                line.addView(Ui.text(this, right, 13, up.attempts == 0 ? Ui.SUB : Ui.GOLD, false));
                card.addView(line);
            }
            content.addView(card);
        }
    }

    private View block(String value, String label) {
        LinearLayout box = Ui.column(this);
        box.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        box.setGravity(Gravity.CENTER_HORIZONTAL);
        box.addView(Ui.text(this, value, 20, Ui.PRIMARY, true));
        box.addView(Ui.text(this, label, 12, Ui.SUB, false));
        return box;
    }
}
