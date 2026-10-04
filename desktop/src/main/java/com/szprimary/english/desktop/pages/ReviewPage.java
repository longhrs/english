package com.szprimary.english.desktop.pages;

import com.szprimary.english.core.model.Word;
import com.szprimary.english.core.progress.ReviewScheduler;
import com.szprimary.english.core.progress.WordProgress;
import com.szprimary.english.desktop.ui.Navigator;
import com.szprimary.english.desktop.ui.Card;
import com.szprimary.english.desktop.ui.HBox;
import com.szprimary.english.desktop.ui.RoundButton;
import com.szprimary.english.desktop.ui.Ui;
import com.szprimary.english.desktop.ui.VBox;

import java.util.List;

import javax.swing.JComponent;

/** 复习中心：今日复习与错题本。 */
public final class ReviewPage extends BasePage {

    private String tab;

    public ReviewPage(Navigator nav, String tab) {
        super(nav);
        this.tab = "wrong".equals(tab) ? "wrong" : "due";
    }

    @Override
    public String title() {
        return "复习中心";
    }

    @Override
    public void build(VBox content) {
        long now = System.currentTimeMillis();
        List<String> dueKeys = repo.progress().dueForReview(now, 50);
        List<String> wrongKeys = repo.progress().wrongBook();
        final boolean due = "due".equals(tab);
        List<Word> words = repo.wordsForKeys(due ? dueKeys : wrongKeys);

        HBox tabs = Ui.row(8);
        tabs.add(tabButton("今日复习 " + dueKeys.size(), "due"));
        tabs.add(tabButton("错题本 " + wrongKeys.size(), "wrong"));
        content.add(tabs);

        Card intro = Ui.card();
        intro.add(Ui.text(due ? "间隔复习" : "错题本", 16, Ui.TEXT, true));
        intro.add(Ui.spacer(4));
        intro.add(Ui.body(due
                ? "答对的词会进入下一个复习盒，间隔 1、2、4、7 天再出现；答错会回到第 1 盒，第二天继续复习。"
                : "练习中答错的词会自动进入错题本，连续答对两次后自动移出。"));
        content.add(intro);

        if (words.isEmpty()) {
            Card empty = Ui.card();
            empty.add(Ui.body(due ? "今天没有到期的复习内容，去学习新单元吧。" : "错题本是空的，保持下去！"));
            content.add(empty);
            return;
        }
        content.add(Ui.primary(due ? "开始今日复习（" + words.size() + " 个词）"
                : "开始错题练习（" + words.size() + " 个词）", new Runnable() {
            @Override
            public void run() {
                nav.push(QuizPage.forReview(nav, due ? "review" : "wrong"));
            }
        }));
        Card list = Ui.card();
        for (int i = 0; i < words.size(); i++) {
            Word word = words.get(i);
            WordProgress wp = repo.progress().peekWord(word.key());
            String state = wp == null ? "新词" : ("第 " + Math.min(wp.box, ReviewScheduler.MAX_BOX) + " 盒");
            list.add(wordRow(word.en + "  " + word.ipa, word.pos + " " + word.cn, state, Ui.SUB, false));
            if (i < words.size() - 1) {
                list.add(Ui.divider());
            }
        }
        content.add(list);
    }

    private JComponent tabButton(String label, final String key) {
        Runnable action = new Runnable() {
            @Override
            public void run() {
                tab = key;
                nav.refreshToTop();
            }
        };
        RoundButton b = tab.equals(key) ? Ui.primary(label, action) : Ui.outlined(label, Ui.SUB, action);
        return Ui.weight(b, 1f);
    }
}
