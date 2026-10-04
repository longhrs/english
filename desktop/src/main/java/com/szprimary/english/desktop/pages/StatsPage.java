package com.szprimary.english.desktop.pages;

import com.szprimary.english.core.model.Grade;
import com.szprimary.english.core.model.Unit;
import com.szprimary.english.core.progress.ProgressManager;
import com.szprimary.english.core.progress.UnitProgress;
import com.szprimary.english.desktop.ui.Navigator;
import com.szprimary.english.desktop.ui.Card;
import com.szprimary.english.desktop.ui.HBox;
import com.szprimary.english.desktop.ui.Ui;
import com.szprimary.english.desktop.ui.VBox;

import java.util.List;

/** 学习统计：总体情况与各年级、各单元成绩。 */
public final class StatsPage extends BasePage {

    public StatsPage(Navigator nav) {
        super(nav);
    }

    @Override
    public String title() {
        return "学习统计";
    }

    @Override
    public void build(VBox content) {
        ProgressManager progress = repo.progress();
        long now = System.currentTimeMillis();
        Card summary = Ui.card();
        summary.add(Ui.text("总体情况", 17, Ui.TEXT, true));
        summary.add(Ui.spacer(10));
        HBox row = Ui.row(8);
        row.add(statBlock(String.valueOf(progress.studyDayCount()), "学习天数", 22));
        row.add(statBlock(String.valueOf(progress.streakDays(now)), "连续天数", 22));
        row.add(statBlock(String.valueOf(progress.totalAnswered()), "累计答题", 22));
        row.add(statBlock(progress.accuracyPercent() + "%", "正确率", 22));
        row.add(statBlock(String.valueOf(progress.learnedWordCount()), "已学词", 22));
        row.add(statBlock(String.valueOf(progress.masteredWordCount()), "已掌握", 22));
        summary.add(row);
        content.add(summary);

        List<Grade> grades = repo.curriculum().grades;
        for (int i = 0; i < grades.size(); i++) {
            Grade grade = grades.get(i);
            Card card = Ui.card();
            HBox head = Ui.row(10);
            VBox titles = Ui.column(2);
            titles.add(Ui.text(grade.title, 16, Ui.TEXT, true));
            titles.add(Ui.text(grade.wordCount() + " 个词汇", 12, Ui.SUB, false));
            head.add(Ui.weight(titles, 1f));
            int percent = repo.gradeLearnedPercent(grade);
            head.add(Ui.label(percent + "%", 18, percent > 0 ? Ui.PRIMARY : Ui.SUB, true));
            card.add(head);
            card.add(Ui.progress(percent));
            for (int j = 0; j < grade.units.size(); j++) {
                Unit unit = grade.units.get(j);
                UnitProgress up = progress.unit(unit.id);
                HBox line = Ui.row(10);
                line.setBorder(new javax.swing.border.EmptyBorder(4, 0, 0, 0));
                line.add(Ui.weight(Ui.text(unit.displayTitle() + " · " + unit.titleCn, 13, Ui.SUB, false), 1f));
                String right = up.attempts == 0 ? "未练习" : (up.bestScore + " 分 " + Ui.stars(up.stars()));
                line.add(Ui.label(right, 13, up.attempts == 0 ? Ui.SUB : Ui.GOLD, false));
                card.add(line);
            }
            content.add(card);
        }
    }
}
