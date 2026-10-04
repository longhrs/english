package com.szprimary.english.desktop.pages;

import com.szprimary.english.core.model.Curriculum;
import com.szprimary.english.core.model.Grade;
import com.szprimary.english.core.progress.ProgressManager;
import com.szprimary.english.desktop.ui.Navigator;
import com.szprimary.english.desktop.ui.Card;
import com.szprimary.english.desktop.ui.HBox;
import com.szprimary.english.desktop.ui.Ui;
import com.szprimary.english.desktop.ui.VBox;

import java.util.List;

import javax.swing.JComponent;

/** 首页：学习概览、年级入口、统计与设置。 */
public final class HomePage extends BasePage {

    public HomePage(Navigator nav) {
        super(nav);
    }

    @Override
    public String title() {
        return "深圳小学英语";
    }

    @Override
    public String subtitle() {
        return "知识引导 · 练习验证 · 一至六年级 · 完全离线";
    }

    @Override
    public void build(VBox content) {
        if (repo.loadError() != null) {
            Card error = Ui.card();
            error.add(Ui.text("课程内容加载失败", 16, Ui.RED, true));
            error.add(Ui.hint(repo.loadError()));
            content.add(error);
        }
        content.add(overviewCard());
        content.add(Ui.text("选择年级", 17, Ui.TEXT, true));
        content.add(gradeGrid());
        content.add(bottomEntries());
    }

    private JComponent overviewCard() {
        ProgressManager progress = repo.progress();
        Curriculum curriculum = repo.curriculum();
        long now = System.currentTimeMillis();
        Card card = Ui.card();
        card.add(Ui.text("学习概览", 17, Ui.TEXT, true));
        card.add(Ui.spacer(10));
        HBox stats = Ui.row(8);
        stats.add(statBlock(String.valueOf(progress.streakDays(now)), "连续学习(天)", 24));
        stats.add(statBlock(String.valueOf(progress.learnedWordCount()), "已学单词", 24));
        stats.add(statBlock(progress.accuracyPercent() + "%", "练习正确率", 24));
        card.add(stats);
        int total = curriculum.totalWords();
        int learned = progress.learnedWordCount();
        int percent = total == 0 ? 0 : Math.round(learned * 100f / total);
        card.add(Ui.spacer(12));
        card.add(Ui.hint("词汇总进度  " + learned + " / " + total + "  (" + percent + "%)"));
        card.add(Ui.progress(percent));
        card.add(Ui.spacer(6));
        int dueCount = progress.dueForReview(now, 50).size();
        int wrongCount = progress.wrongBook().size();
        HBox actions = Ui.row(10);
        actions.add(Ui.weight(Ui.soft("今日复习 " + dueCount, new Runnable() {
            @Override
            public void run() {
                nav.push(new ReviewPage(nav, "due"));
            }
        }), 1f));
        actions.add(Ui.weight(Ui.soft("错题本 " + wrongCount, new Runnable() {
            @Override
            public void run() {
                nav.push(new ReviewPage(nav, "wrong"));
            }
        }), 1f));
        card.add(actions);
        return card;
    }

    /** 宽屏上一行三个年级。 */
    private JComponent gradeGrid() {
        VBox grid = Ui.column(12);
        List<Grade> grades = repo.curriculum().grades;
        for (int i = 0; i < grades.size(); i += 3) {
            HBox row = Ui.row(12);
            for (int j = i; j < i + 3; j++) {
                if (j < grades.size()) {
                    row.add(Ui.weight(gradeCard(grades.get(j)), 1f));
                } else {
                    row.add(Ui.weight(Ui.column(0), 1f));
                }
            }
            grid.add(row);
        }
        return grid;
    }

    private JComponent gradeCard(final Grade grade) {
        Card card = new Card(Ui.CARD, Ui.LINE, 14, 16, 4);
        card.add(Ui.text(grade.title, 19, Ui.TEXT, true));
        card.add(Ui.text(grade.units.size() + " 个单元 · " + grade.wordCount() + " 词", 12, Ui.SUB, false));
        int percent = repo.gradeLearnedPercent(grade);
        card.add(Ui.progress(percent));
        card.add(Ui.text(percent + "% 已学", 12, percent > 0 ? Ui.GREEN : Ui.SUB, false));
        Ui.clickable(card, new Runnable() {
            @Override
            public void run() {
                nav.push(new UnitsPage(nav, grade.grade));
            }
        });
        return card;
    }

    private JComponent bottomEntries() {
        Card card = Ui.card();
        card.add(entry("学习统计", "各年级进度、正确率与掌握情况", new Runnable() {
            @Override
            public void run() {
                nav.push(new StatsPage(nav));
            }
        }));
        card.add(Ui.divider());
        card.add(entry("关于与设置", "使用说明、内容来源、朗读设置、清空学习进度", new Runnable() {
            @Override
            public void run() {
                nav.push(new AboutPage(nav));
            }
        }));
        return card;
    }
}
