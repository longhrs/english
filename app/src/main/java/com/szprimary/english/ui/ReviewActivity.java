package com.szprimary.english.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.szprimary.english.core.model.Word;
import com.szprimary.english.core.progress.ReviewScheduler;
import com.szprimary.english.core.progress.WordProgress;

import java.util.List;

/** 今日复习与错题本。 */
public final class ReviewActivity extends Activity {

    private AppRepo repo;
    private String tab = "due";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repo = AppRepo.get(this);
        getWindow().setStatusBarColor(Ui.PRIMARY_DARK);
        String requested = getIntent().getStringExtra("tab");
        if (requested != null) {
            tab = requested;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        build();
    }

    private void build() {
        long now = System.currentTimeMillis();
        List<String> dueKeys = repo.progress().dueForReview(now, 50);
        List<String> wrongKeys = repo.progress().wrongBook();
        boolean due = "due".equals(tab);
        List<Word> words = repo.wordsForKeys(due ? dueKeys : wrongKeys);

        LinearLayout content = Ui.page(this, "复习中心", null);

        LinearLayout tabs = Ui.row(this);
        tabs.addView(tabButton("今日复习 " + dueKeys.size(), "due"));
        tabs.addView(tabButton("错题本 " + wrongKeys.size(), "wrong"));
        content.addView(tabs);
        content.addView(Ui.spacer(this, 12));

        LinearLayout intro = Ui.card(this);
        intro.addView(Ui.text(this, due ? "间隔复习" : "错题本", 16, Ui.TEXT, true));
        intro.addView(Ui.spacer(this, 4));
        intro.addView(Ui.body(this, due
                ? "答对的词会进入下一个复习盒，间隔 1、2、4、7 天再出现；答错会回到第 1 盒，第二天继续复习。"
                : "练习中答错的词会自动进入错题本，连续答对两次后自动移出。"));
        content.addView(intro);

        if (words.isEmpty()) {
            LinearLayout empty = Ui.card(this);
            empty.addView(Ui.body(this, due
                    ? "今天没有到期的复习内容，去学习新单元吧。"
                    : "错题本是空的，保持下去！"));
            content.addView(empty);
            return;
        }

        Button start = Ui.primary(this, due ? "开始今日复习（" + words.size() + " 个词）"
                : "开始错题练习（" + words.size() + " 个词）");
        final boolean isDue = due;
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReviewActivity.this, QuizActivity.class);
                intent.putExtra("mode", isDue ? "review" : "wrong");
                startActivity(intent);
            }
        });
        content.addView(start);
        content.addView(Ui.spacer(this, 12));

        LinearLayout list = Ui.card(this);
        for (int i = 0; i < words.size(); i++) {
            Word word = words.get(i);
            WordProgress wp = repo.progress().peekWord(word.key());
            LinearLayout row = Ui.row(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, Ui.dp(this, 8), 0, Ui.dp(this, 8));
            LinearLayout texts = Ui.column(this);
            texts.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            texts.addView(Ui.text(this, word.en + "  " + word.ipa, 15, Ui.TEXT, true));
            texts.addView(Ui.text(this, word.pos + " " + word.cn, 13, Ui.SUB, false));
            row.addView(texts);
            String state = wp == null ? "新词" : ("第 " + Math.min(wp.box, ReviewScheduler.MAX_BOX) + " 盒");
            row.addView(Ui.text(this, state, 12, Ui.SUB, false));
            list.addView(row);
            if (i < words.size() - 1) {
                View line = new View(this);
                line.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, Math.max(1, Ui.dp(this, 0.6f))));
                line.setBackgroundColor(Ui.LINE);
                list.addView(line);
            }
        }
        content.addView(list);
    }

    private View tabButton(String label, final String key) {
        boolean active = tab.equals(key);
        Button button = Ui.button(this, label, active ? Ui.PRIMARY : Ui.CARD,
                active ? 0xFFFFFFFF : Ui.SUB);
        if (!active) {
            button.setBackground(Ui.outlined(this, Ui.LINE, 12));
        }
        button.setTextSize(14);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        params.rightMargin = Ui.dp(this, 6);
        params.topMargin = 0;
        button.setLayoutParams(params);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tab = key;
                build();
            }
        });
        return button;
    }
}
