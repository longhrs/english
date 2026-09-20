package com.szprimary.english.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.szprimary.english.core.model.Curriculum;
import com.szprimary.english.core.model.Grade;
import com.szprimary.english.core.progress.ProgressManager;

import java.util.List;

/** 首页：学习概览、年级入口、复习与错题入口。 */
public final class HomeActivity extends Activity {

    private AppRepo repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repo = AppRepo.get(this);
        getWindow().setStatusBarColor(Ui.PRIMARY_DARK);
    }

    @Override
    protected void onResume() {
        super.onResume();
        build();
    }

    private void build() {
        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(Ui.BG);
        root.addView(hero());

        android.widget.ScrollView scroll = new android.widget.ScrollView(this);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        LinearLayout content = Ui.column(this);
        int pad = Ui.dp(this, 14);
        content.setPadding(pad, pad, pad, Ui.dp(this, 28));
        scroll.addView(content);
        root.addView(scroll);
        setContentView(root);

        if (repo.loadError() != null) {
            LinearLayout error = Ui.card(this);
            error.addView(Ui.text(this, "课程内容加载失败", 16, Ui.RED, true));
            error.addView(Ui.hint(this, String.valueOf(repo.loadError())));
            content.addView(error);
        }

        content.addView(overviewCard());
        content.addView(Ui.text(this, "选择年级", 17, Ui.TEXT, true));
        content.addView(Ui.spacer(this, 8));
        content.addView(gradeGrid());
        content.addView(Ui.spacer(this, 4));
        content.addView(bottomEntries());
    }

    private View hero() {
        LinearLayout hero = Ui.column(this);
        hero.setBackgroundColor(Ui.PRIMARY);
        int pad = Ui.dp(this, 18);
        hero.setPadding(pad, Ui.dp(this, 22), pad, Ui.dp(this, 20));
        hero.addView(Ui.text(this, "深圳小学英语", 24, 0xFFFFFFFF, true));
        hero.addView(Ui.spacer(this, 4));
        hero.addView(Ui.text(this, "知识引导 · 练习验证 · 一至六年级 · 完全离线", 13, 0xCCFFFFFF, false));
        return hero;
    }

    private View overviewCard() {
        ProgressManager progress = repo.progress();
        Curriculum curriculum = repo.curriculum();
        long now = System.currentTimeMillis();

        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, "学习概览", 17, Ui.TEXT, true));
        card.addView(Ui.spacer(this, 10));

        LinearLayout stats = Ui.row(this);
        stats.addView(statBlock(String.valueOf(progress.streakDays(now)), "连续学习(天)"));
        stats.addView(statBlock(progress.learnedWordCount() + "", "已学单词"));
        stats.addView(statBlock(progress.accuracyPercent() + "%", "练习正确率"));
        card.addView(stats);

        int total = curriculum.totalWords();
        int learned = progress.learnedWordCount();
        int percent = total == 0 ? 0 : Math.round(learned * 100f / total);
        card.addView(Ui.spacer(this, 10));
        card.addView(Ui.hint(this, "词汇总进度  " + learned + " / " + total + "  (" + percent + "%)"));
        card.addView(Ui.progress(this, percent));

        LinearLayout actions = Ui.row(this);
        actions.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        int dueCount = progress.dueForReview(now, 50).size();
        int wrongCount = progress.wrongBook().size();
        actions.addView(flexButton(Ui.soft(this, "今日复习 " + dueCount), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, ReviewActivity.class));
            }
        }, Ui.dp(this, 4)));
        actions.addView(flexButton(Ui.soft(this, "错题本 " + wrongCount), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ReviewActivity.class);
                intent.putExtra("tab", "wrong");
                startActivity(intent);
            }
        }, Ui.dp(this, 4)));
        card.addView(actions);
        return card;
    }

    private View statBlock(String value, String label) {
        LinearLayout block = Ui.column(this);
        block.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        block.setGravity(Gravity.CENTER_HORIZONTAL);
        block.addView(Ui.text(this, value, 22, Ui.PRIMARY, true));
        block.addView(Ui.text(this, label, 12, Ui.SUB, false));
        return block;
    }

    private View flexButton(Button button, View.OnClickListener listener, int margin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        params.leftMargin = margin;
        params.rightMargin = margin;
        params.topMargin = Ui.dp(this, 10);
        button.setLayoutParams(params);
        button.setOnClickListener(listener);
        return button;
    }

    private View gradeGrid() {
        LinearLayout grid = Ui.column(this);
        List<Grade> grades = repo.curriculum().grades;
        for (int i = 0; i < grades.size(); i += 2) {
            LinearLayout row = Ui.row(this);
            row.addView(gradeCard(grades.get(i)));
            if (i + 1 < grades.size()) {
                row.addView(gradeCard(grades.get(i + 1)));
            } else {
                View filler = new View(this);
                filler.setLayoutParams(new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.MATCH_PARENT, 1f));
                row.addView(filler);
            }
            grid.addView(row);
        }
        return grid;
    }

    private View gradeCard(final Grade grade) {
        LinearLayout card = Ui.column(this);
        card.setBackground(Ui.rounded(this, Ui.CARD, 14));
        int pad = Ui.dp(this, 14);
        card.setPadding(pad, pad, pad, pad);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        params.rightMargin = Ui.dp(this, 6);
        params.leftMargin = Ui.dp(this, 6);
        params.bottomMargin = Ui.dp(this, 12);
        card.setLayoutParams(params);

        card.addView(Ui.text(this, grade.title, 18, Ui.TEXT, true));
        card.addView(Ui.text(this, grade.units.size() + " 个单元 · " + grade.wordCount() + " 词", 12, Ui.SUB, false));
        int percent = repo.gradeLearnedPercent(grade);
        card.addView(Ui.progress(this, percent));
        card.addView(Ui.text(this, percent + "% 已学", 12, percent > 0 ? Ui.GREEN : Ui.SUB, false));

        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, UnitsActivity.class);
                intent.putExtra("grade", grade.grade);
                startActivity(intent);
            }
        });
        return card;
    }

    private View bottomEntries() {
        LinearLayout card = Ui.card(this);
        card.addView(entry("学习统计", "各年级进度、正确率与掌握情况", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, StatsActivity.class));
            }
        }));
        card.addView(Ui.divider(this));
        card.addView(entry("关于与设置", "使用说明、内容来源、清空学习进度", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, AboutActivity.class));
            }
        }));
        return card;
    }

    private View entry(String title, String desc, View.OnClickListener listener) {
        LinearLayout row = Ui.row(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout texts = Ui.column(this);
        texts.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        texts.addView(Ui.text(this, title, 16, Ui.TEXT, true));
        texts.addView(Ui.text(this, desc, 12, Ui.SUB, false));
        row.addView(texts);
        TextView arrow = Ui.text(this, "›", 22, Ui.SUB, false);
        row.addView(arrow);
        row.setOnClickListener(listener);
        return row;
    }
}
