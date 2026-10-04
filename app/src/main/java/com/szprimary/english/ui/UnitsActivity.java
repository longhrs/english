package com.szprimary.english.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.szprimary.english.core.model.Grade;
import com.szprimary.english.core.model.Unit;
import com.szprimary.english.core.progress.UnitProgress;

/** 某个年级的单元列表。 */
public final class UnitsActivity extends Activity {

    private AppRepo repo;
    private int gradeNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repo = AppRepo.get(this);
        gradeNumber = getIntent().getIntExtra("grade", 1);
        getWindow().setStatusBarColor(Ui.PRIMARY_DARK);
    }

    @Override
    protected void onResume() {
        super.onResume();
        build();
    }

    private void build() {
        Grade grade = repo.curriculum().grade(gradeNumber);
        String title = grade == null ? "年级" : grade.title;
        LinearLayout content = Ui.page(this, title, repo.gradeLearnedPercent(grade == null
                ? new Grade(gradeNumber, title, "", new java.util.ArrayList<Unit>()) : grade) + "% 已学");
        if (grade == null) {
            content.addView(Ui.body(this, "没有找到该年级的内容。"));
            return;
        }

        LinearLayout intro = Ui.card(this);
        intro.addView(Ui.text(this, grade.title + " 学习目标", 16, Ui.TEXT, true));
        intro.addView(Ui.spacer(this, 4));
        intro.addView(Ui.body(this, grade.subtitle));
        content.addView(intro);

        String lastBook = "";
        for (int i = 0; i < grade.units.size(); i++) {
            Unit unit = grade.units.get(i);
            if (!unit.book.equals(lastBook)) {
                String heading = bookHeading(unit.book);
                if (heading != null) {
                    content.addView(Ui.text(this, heading, 16, Ui.PRIMARY, true));
                    content.addView(Ui.spacer(this, 6));
                }
                lastBook = unit.book;
            }
            content.addView(unitCard(unit));
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

    private View unitCard(final Unit unit) {
        UnitProgress up = repo.progress().unit(unit.id);
        LinearLayout card = Ui.card(this);

        LinearLayout titleRow = Ui.row(this);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout titles = Ui.column(this);
        titles.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        titles.addView(Ui.text(this, unit.displayTitle(), 17, Ui.TEXT, true));
        titles.addView(Ui.text(this, unit.titleCn + " · " + unit.topic, 13, Ui.SUB, false));
        titleRow.addView(titles);
        titleRow.addView(Ui.text(this, Ui.stars(up.stars()), 16, Ui.GOLD, false));
        card.addView(titleRow);

        card.addView(Ui.spacer(this, 8));
        card.addView(Ui.hint(this, unit.words.size() + " 个词汇 · " + unit.patterns.size()
                + " 条句型 · " + unit.grammar.size() + " 个语法点"
                + (up.attempts > 0 ? ("  |  最好成绩 " + up.bestScore + " 分") : "")));

        int percent = repo.unitLearnedPercent(unit);
        card.addView(Ui.progress(this, percent));
        card.addView(Ui.text(this, "知识引导进度 " + percent + "%", 12, percent > 0 ? Ui.GREEN : Ui.SUB, false));

        LinearLayout buttons = Ui.row(this);
        Button learn = Ui.soft(this, "知识引导");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp.rightMargin = Ui.dp(this, 5);
        lp.topMargin = Ui.dp(this, 10);
        learn.setLayoutParams(lp);
        learn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UnitsActivity.this, LearnActivity.class);
                intent.putExtra("unitId", unit.id);
                startActivity(intent);
            }
        });
        buttons.addView(learn);

        Button quiz = Ui.primary(this, "练习验证");
        LinearLayout.LayoutParams qp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        qp.leftMargin = Ui.dp(this, 5);
        qp.topMargin = Ui.dp(this, 10);
        quiz.setLayoutParams(qp);
        quiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UnitsActivity.this, QuizActivity.class);
                intent.putExtra("mode", "unit");
                intent.putExtra("unitId", unit.id);
                startActivity(intent);
            }
        });
        buttons.addView(quiz);
        card.addView(buttons);
        return card;
    }
}
