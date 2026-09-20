package com.szprimary.english.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.szprimary.english.core.model.Example;
import com.szprimary.english.core.model.GrammarNote;
import com.szprimary.english.core.model.SentencePattern;
import com.szprimary.english.core.model.Unit;
import com.szprimary.english.core.model.Word;
import com.szprimary.english.core.progress.WordProgress;

/** 知识引导：词汇卡、核心句型、语法讲解。 */
public final class LearnActivity extends Activity {

    private AppRepo repo;
    private Unit unit;
    private int section;
    private int wordIndex;
    private boolean revealed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repo = AppRepo.get(this);
        unit = repo.curriculum().unit(getIntent().getStringExtra("unitId"));
        getWindow().setStatusBarColor(Ui.PRIMARY_DARK);
        build();
    }

    private void build() {
        if (unit == null) {
            LinearLayout content = Ui.page(this, "知识引导", null);
            content.addView(Ui.body(this, "没有找到该单元。"));
            return;
        }
        LinearLayout content = Ui.page(this, unit.displayTitle(), unit.titleCn);
        content.addView(switcher());
        if (section == 0) {
            content.addView(overviewCard());
            content.addView(wordCard());
            content.addView(wordListCard());
        } else if (section == 1) {
            for (int i = 0; i < unit.patterns.size(); i++) {
                content.addView(patternCard(unit.patterns.get(i)));
            }
        } else {
            for (int i = 0; i < unit.grammar.size(); i++) {
                content.addView(grammarCard(unit.grammar.get(i)));
            }
            if (!unit.tips.isEmpty()) {
                LinearLayout tips = Ui.card(this);
                tips.addView(Ui.text(this, "学习小贴士", 16, Ui.ACCENT, true));
                for (int i = 0; i < unit.tips.size(); i++) {
                    tips.addView(Ui.spacer(this, 6));
                    tips.addView(Ui.body(this, "· " + unit.tips.get(i)));
                }
                content.addView(tips);
            }
        }

        Button start = Ui.primary(this, "开始本单元练习");
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LearnActivity.this, QuizActivity.class);
                intent.putExtra("mode", "unit");
                intent.putExtra("unitId", unit.id);
                startActivity(intent);
            }
        });
        content.addView(start);
    }

    private View switcher() {
        LinearLayout row = Ui.row(this);
        row.addView(tab("词汇 " + unit.words.size(), 0));
        row.addView(tab("句型 " + unit.patterns.size(), 1));
        row.addView(tab("语法 " + unit.grammar.size(), 2));
        LinearLayout wrapper = Ui.column(this);
        wrapper.addView(row);
        wrapper.addView(Ui.spacer(this, 10));
        return wrapper;
    }

    private View tab(String label, final int index) {
        boolean active = section == index;
        Button button = Ui.button(this, label, active ? Ui.PRIMARY : 0x00000000,
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
                section = index;
                revealed = false;
                build();
            }
        });
        return button;
    }

    private View overviewCard() {
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, "本单元学什么", 16, Ui.TEXT, true));
        card.addView(Ui.spacer(this, 6));
        card.addView(Ui.body(this, unit.overview));
        if (unit.phonics != null) {
            card.addView(Ui.divider(this));
            card.addView(Ui.text(this, "语音重点：" + unit.phonics.focus, 15, Ui.PRIMARY, true));
            card.addView(Ui.spacer(this, 4));
            card.addView(Ui.body(this, unit.phonics.tip));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < unit.phonics.examples.size(); i++) {
                if (i > 0) {
                    sb.append("   ");
                }
                sb.append(unit.phonics.examples.get(i));
            }
            card.addView(Ui.spacer(this, 4));
            card.addView(Ui.hint(this, sb.toString()));
        }
        return card;
    }

    private View wordCard() {
        if (unit.words.isEmpty()) {
            return Ui.card(this);
        }
        if (wordIndex >= unit.words.size()) {
            wordIndex = 0;
        }
        final Word word = unit.words.get(wordIndex);
        WordProgress wp = repo.progress().peekWord(word.key());
        final boolean learned = wp != null && wp.learned;

        LinearLayout card = Ui.card(this);
        LinearLayout top = Ui.row(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout left = Ui.column(this);
        left.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        left.addView(Ui.text(this, word.en, 30, Ui.TEXT, true));
        left.addView(Ui.text(this, word.ipa + "   " + word.pos, 15, Ui.SUB, false));
        top.addView(left);
        Button speak = Ui.soft(this, "🔊 朗读");
        speak.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakOrWarn(word.en);
            }
        });
        top.addView(speak);
        card.addView(top);

        card.addView(Ui.spacer(this, 12));
        if (revealed) {
            card.addView(Ui.text(this, word.cn, 20, Ui.PRIMARY, true));
            card.addView(Ui.spacer(this, 8));
            card.addView(Ui.body(this, word.exEn));
            card.addView(Ui.hint(this, word.exCn));
            Button speakSentence = Ui.soft(this, "🔊 朗读例句");
            speakSentence.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    speakOrWarn(word.exEn);
                }
            });
            card.addView(speakSentence);
        } else {
            Button reveal = Ui.soft(this, "显示中文释义与例句");
            reveal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    revealed = true;
                    build();
                }
            });
            card.addView(reveal);
        }

        card.addView(Ui.divider(this));
        card.addView(Ui.hint(this, "第 " + (wordIndex + 1) + " / " + unit.words.size() + " 个词"));

        LinearLayout nav = Ui.row(this);
        nav.addView(navButton("上一个", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordIndex = (wordIndex - 1 + unit.words.size()) % unit.words.size();
                revealed = false;
                build();
            }
        }, false));
        nav.addView(navButton(learned ? "✓ 已掌握" : "标记已掌握", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (learned) {
                    repo.progress().unmarkLearned(unit.id, word.key());
                } else {
                    repo.progress().markLearned(unit.id, word.key(), System.currentTimeMillis());
                }
                repo.progress().save();
                build();
            }
        }, learned));
        nav.addView(navButton("下一个", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordIndex = (wordIndex + 1) % unit.words.size();
                revealed = false;
                build();
            }
        }, false));
        card.addView(nav);
        return card;
    }

    private View navButton(String label, View.OnClickListener listener, boolean highlight) {
        Button button = Ui.button(this, label, highlight ? Ui.GREEN : Ui.SOFT,
                highlight ? 0xFFFFFFFF : Ui.PRIMARY);
        button.setTextSize(14);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        params.rightMargin = Ui.dp(this, 4);
        params.leftMargin = Ui.dp(this, 4);
        params.topMargin = Ui.dp(this, 10);
        button.setLayoutParams(params);
        button.setOnClickListener(listener);
        return button;
    }

    private View wordListCard() {
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, "本单元词表", 16, Ui.TEXT, true));
        for (int i = 0; i < unit.words.size(); i++) {
            final int index = i;
            Word word = unit.words.get(i);
            WordProgress wp = repo.progress().peekWord(word.key());
            boolean learned = wp != null && wp.learned;

            LinearLayout row = Ui.row(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, Ui.dp(this, 8), 0, Ui.dp(this, 8));
            LinearLayout texts = Ui.column(this);
            texts.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            texts.addView(Ui.text(this, word.en + "  " + word.ipa, 15,
                    index == wordIndex ? Ui.PRIMARY : Ui.TEXT, index == wordIndex));
            texts.addView(Ui.text(this, word.pos + " " + word.cn, 13, Ui.SUB, false));
            row.addView(texts);
            row.addView(Ui.text(this, learned ? "✓" : "", 16, Ui.GREEN, true));
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    wordIndex = index;
                    revealed = true;
                    section = 0;
                    build();
                }
            });
            card.addView(row);
            if (i < unit.words.size() - 1) {
                View line = new View(this);
                line.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, Math.max(1, Ui.dp(this, 0.6f))));
                line.setBackgroundColor(Ui.LINE);
                card.addView(line);
            }
        }
        return card;
    }

    private View patternCard(final SentencePattern pattern) {
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, pattern.en, 18, Ui.TEXT, true));
        card.addView(Ui.spacer(this, 4));
        card.addView(Ui.text(this, pattern.cn, 15, Ui.PRIMARY, false));
        card.addView(Ui.divider(this));
        for (int i = 0; i < pattern.examples.size(); i++) {
            final Example example = pattern.examples.get(i);
            LinearLayout row = Ui.row(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout texts = Ui.column(this);
            texts.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            texts.addView(Ui.body(this, example.en));
            texts.addView(Ui.hint(this, example.cn));
            row.addView(texts);
            Button speak = Ui.soft(this, "🔊");
            speak.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            speak.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    speakOrWarn(example.en);
                }
            });
            row.addView(speak);
            card.addView(row);
            if (i < pattern.examples.size() - 1) {
                card.addView(Ui.spacer(this, 6));
            }
        }
        return card;
    }

    private View grammarCard(GrammarNote note) {
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, note.title, 17, Ui.TEXT, true));
        card.addView(Ui.spacer(this, 6));
        card.addView(Ui.body(this, note.explain));
        if (!note.examples.isEmpty()) {
            card.addView(Ui.divider(this));
            for (int i = 0; i < note.examples.size(); i++) {
                Example example = note.examples.get(i);
                card.addView(Ui.body(this, "· " + example.en));
                card.addView(Ui.hint(this, "   " + example.cn));
                if (i < note.examples.size() - 1) {
                    card.addView(Ui.spacer(this, 6));
                }
            }
        }
        return card;
    }

    private void speakOrWarn(String text) {
        if (repo.speaker().isReady()) {
            repo.speaker().speak(text);
        } else {
            Ui.toast(this, "本机暂无英语语音引擎，可在系统设置中安装后使用朗读");
        }
    }
}
