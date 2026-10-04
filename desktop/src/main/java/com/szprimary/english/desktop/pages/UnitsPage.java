package com.szprimary.english.desktop.pages;

import com.szprimary.english.core.model.Grade;
import com.szprimary.english.core.model.Unit;
import com.szprimary.english.core.progress.UnitProgress;
import com.szprimary.english.desktop.ui.Navigator;
import com.szprimary.english.desktop.ui.Card;
import com.szprimary.english.desktop.ui.HBox;
import com.szprimary.english.desktop.ui.Ui;
import com.szprimary.english.desktop.ui.VBox;

import javax.swing.JComponent;

/** 某个年级的单元列表。 */
public final class UnitsPage extends BasePage {

    private final int gradeNumber;
    private String subtitle;

    public UnitsPage(Navigator nav, int gradeNumber) {
        super(nav);
        this.gradeNumber = gradeNumber;
    }

    @Override
    public String title() {
        Grade grade = repo.curriculum().grade(gradeNumber);
        return grade == null ? "年级" : grade.title;
    }

    @Override
    public String subtitle() {
        return subtitle;
    }

    @Override
    public void build(VBox content) {
        Grade grade = repo.curriculum().grade(gradeNumber);
        if (grade == null) {
            subtitle = null;
            content.add(Ui.body("没有找到该年级的内容。"));
            return;
        }
        subtitle = repo.gradeLearnedPercent(grade) + "% 已学";
        Card intro = Ui.card();
        intro.add(Ui.text(grade.title + " 学习目标", 16, Ui.TEXT, true));
        intro.add(Ui.spacer(4));
        intro.add(Ui.body(grade.subtitle));
        content.add(intro);
        String lastBook = "";
        for (int i = 0; i < grade.units.size(); i++) {
            Unit unit = grade.units.get(i);
            if (!unit.book.equals(lastBook)) {
                String heading = bookHeading(unit.book);
                if (heading != null) {
                    content.add(Ui.text(heading, 16, Ui.PRIMARY, true));
                }
                lastBook = unit.book;
            }
            content.add(unitCard(unit));
        }
    }

    /** 单元列表里的分组标题：按课本册次分组（只在单元标注了 book 时显示）。 */
    static String bookHeading(String book) {
        if (book == null || book.length() == 0) {
            return null;
        }
        if ("拓展".equals(book)) {
            return "拓展话题（旧版课本内容，可作课外拓展）";
        }
        if (book.length() == 2 && Character.isDigit(book.charAt(0))) {
            String[] grades = {"", "一", "二", "三", "四", "五", "六"};
            int g = book.charAt(0) - '0';
            String half = book.charAt(1) == 'A' ? "上册" : "下册";
            return (g >= 1 && g <= 6 ? grades[g] : String.valueOf(g)) + "年级" + half + "（" + book + "）· 新版课本";
        }
        return book;
    }

    private JComponent unitCard(final Unit unit) {
        UnitProgress up = repo.progress().unit(unit.id);
        Card card = Ui.card();
        HBox titleRow = Ui.row(10);
        VBox titles = Ui.column(2);
        titles.add(Ui.text(unit.displayTitle(), 17, Ui.TEXT, true));
        titles.add(Ui.text(unit.titleCn + " · " + unit.topic, 13, Ui.SUB, false));
        titleRow.add(Ui.weight(titles, 1f));
        titleRow.add(Ui.label(Ui.stars(up.stars()), 17, Ui.GOLD, false));
        card.add(titleRow);
        card.add(Ui.spacer(8));
        card.add(Ui.hint(unit.words.size() + " 个词汇 · " + unit.patterns.size()
                + " 条句型 · " + unit.grammar.size() + " 个语法点"
                + (up.attempts > 0 ? ("  |  最好成绩 " + up.bestScore + " 分") : "")));
        int percent = repo.unitLearnedPercent(unit);
        card.add(Ui.progress(percent));
        card.add(Ui.text("知识引导进度 " + percent + "%", 12, percent > 0 ? Ui.GREEN : Ui.SUB, false));
        card.add(Ui.spacer(10));
        HBox buttons = Ui.row(10);
        buttons.add(Ui.weight(Ui.soft("知识引导", new Runnable() {
            @Override
            public void run() {
                nav.push(new LearnPage(nav, unit.id));
            }
        }), 1f));
        buttons.add(Ui.weight(Ui.primary("练习验证", new Runnable() {
            @Override
            public void run() {
                nav.push(QuizPage.forUnit(nav, unit.id));
            }
        }), 1f));
        card.add(buttons);
        return card;
    }
}
