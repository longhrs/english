package com.szprimary.english.desktop.pages;

import com.szprimary.english.core.quiz.QuestionRecord;
import com.szprimary.english.core.quiz.QuizResult;
import com.szprimary.english.desktop.ui.Navigator;
import com.szprimary.english.desktop.ui.Card;
import com.szprimary.english.desktop.ui.HBox;
import com.szprimary.english.desktop.ui.Ui;
import com.szprimary.english.desktop.ui.VBox;

import java.util.List;

/** 练习报告：得分、星级、用时和错题回顾。 */
public final class ResultPage extends BasePage {

    public ResultPage(Navigator nav) {
        super(nav);
    }

    @Override
    public String title() {
        return "练习报告";
    }

    @Override
    public void build(VBox content) {
        final QuizResult result = repo.lastResult;
        if (result == null) {
            content.add(Ui.body("没有可显示的练习结果。"));
            return;
        }
        Card score = Ui.card();
        score.add(Ui.centered(repo.lastQuizTitle == null ? "练习" : repo.lastQuizTitle, 14, Ui.SUB, false));
        score.add(Ui.spacer(6));
        score.add(Ui.centered(String.valueOf(result.score), 56, result.score >= 60 ? Ui.PRIMARY : Ui.RED, true));
        score.add(Ui.centered("分", 14, Ui.SUB, false));
        score.add(Ui.spacer(6));
        score.add(Ui.centered(Ui.stars(result.stars), 28, Ui.GOLD, false));
        score.add(Ui.spacer(8));
        score.add(Ui.centered("答对 " + result.correct + " / " + result.total
                + " 题 · 用时 " + formatDuration(result.durationMs), 14, Ui.SUB, false));
        score.add(Ui.spacer(8));
        score.add(Ui.centered(result.comment(), 15, Ui.TEXT, false));
        content.add(score);

        List<QuestionRecord> wrong = result.wrongRecords();
        if (wrong.isEmpty()) {
            Card perfect = Ui.card();
            perfect.add(Ui.text("全部答对，没有错题！", 16, Ui.GREEN, true));
            perfect.add(Ui.spacer(4));
            perfect.add(Ui.hint("这些词会进入间隔复习队列，过几天再巩固一次效果最好。"));
            content.add(perfect);
        } else {
            content.add(Ui.text("错题回顾（" + wrong.size() + " 题）", 17, Ui.TEXT, true));
            for (int i = 0; i < wrong.size(); i++) {
                QuestionRecord record = wrong.get(i);
                Card card = Ui.card();
                card.add(Ui.text((i + 1) + ". " + record.question.prompt, 16, Ui.TEXT, true));
                card.add(Ui.spacer(6));
                String mine = record.answer == null ? "" : record.answer.display(record.question);
                card.add(Ui.text("你的答案：" + (mine.trim().length() == 0 ? "未作答" : mine), 14, Ui.RED, false));
                card.add(Ui.text("正确答案：" + record.question.correctText, 14, Ui.GREEN, true));
                card.add(Ui.divider());
                card.add(Ui.body(record.question.explanation));
                content.add(card);
            }
        }

        final String unitId = repo.lastQuizUnitId;
        HBox actions = Ui.row(10);
        actions.add(Ui.weight(Ui.primary("再练一遍", new Runnable() {
            @Override
            public void run() {
                nav.replace(unitId != null ? QuizPage.forUnit(nav, unitId) : QuizPage.forReview(nav, "review"));
            }
        }), 1f));
        int wrongBook = repo.progress().wrongBook().size();
        if (wrongBook > 0) {
            actions.add(Ui.weight(Ui.soft("只练错题（" + wrongBook + " 个词）", new Runnable() {
                @Override
                public void run() {
                    nav.replace(QuizPage.forReview(nav, "wrong"));
                }
            }), 1f));
        }
        actions.add(Ui.weight(Ui.soft("返回", new Runnable() {
            @Override
            public void run() {
                nav.pop();
            }
        }), 1f));
        content.add(actions);
    }

    static String formatDuration(long ms) {
        long seconds = Math.max(0, ms / 1000);
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return minutes > 0 ? (minutes + " 分 " + seconds + " 秒") : (seconds + " 秒");
    }
}
