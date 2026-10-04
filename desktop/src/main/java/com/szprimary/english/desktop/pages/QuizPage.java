package com.szprimary.english.desktop.pages;

import com.szprimary.english.core.model.Unit;
import com.szprimary.english.core.model.Word;
import com.szprimary.english.core.quiz.Answer;
import com.szprimary.english.core.quiz.Question;
import com.szprimary.english.core.quiz.QuestionRecord;
import com.szprimary.english.core.quiz.QuestionType;
import com.szprimary.english.core.quiz.QuizGenerator;
import com.szprimary.english.core.quiz.QuizOptions;
import com.szprimary.english.core.quiz.QuizResult;
import com.szprimary.english.core.quiz.QuizSession;
import com.szprimary.english.desktop.ui.Navigator;
import com.szprimary.english.desktop.ui.Card;
import com.szprimary.english.desktop.ui.HBox;
import com.szprimary.english.desktop.ui.RoundButton;
import com.szprimary.english.desktop.ui.Ui;
import com.szprimary.english.desktop.ui.VBox;
import com.szprimary.english.desktop.ui.WrapBox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/** 练习验证：单元练习、今日复习、错题本练习共用。 */
public final class QuizPage extends BasePage {

    private final String mode;
    private final String unitId;
    private Unit unit;
    private String titleText = "练习验证";
    private final QuizSession session;
    private boolean answered;
    private final List<String> chosenTokens = new ArrayList<String>();
    private List<String> poolTokens = new ArrayList<String>();
    private JTextField input;

    private QuizPage(Navigator nav, String mode, String unitId) {
        super(nav);
        this.mode = mode;
        this.unitId = unitId;
        this.session = createSession();
        resetQuestionState();
    }

    public static QuizPage forUnit(Navigator nav, String unitId) {
        return new QuizPage(nav, "unit", unitId);
    }

    /** mode 为 "review"（今日复习）或 "wrong"（错题本）。 */
    public static QuizPage forReview(Navigator nav, String mode) {
        return new QuizPage(nav, mode, null);
    }

    QuizSession session() {
        return session;
    }

    private QuizSession createSession() {
        long now = System.currentTimeMillis();
        List<Question> questions;
        if ("unit".equals(mode)) {
            unit = repo.curriculum().unit(unitId);
            if (unit == null) {
                return new QuizSession(new ArrayList<Question>(), now);
            }
            titleText = unit.displayTitle() + " 练习";
            QuizOptions options = QuizOptions.of(Math.max(8, unit.words.size()), now).forGrade(unit.grade);
            questions = QuizGenerator.forUnit(unit, repo.distractorPool(unit.grade), options);
        } else {
            List<String> keys;
            if ("wrong".equals(mode)) {
                titleText = "错题本练习";
                keys = repo.progress().wrongBook();
            } else {
                titleText = "今日复习";
                keys = repo.progress().dueForReview(now, 12);
            }
            List<Word> words = repo.wordsForKeys(keys);
            if (words.size() > 12) {
                words = words.subList(0, 12);
            }
            QuizOptions options = QuizOptions.of(Math.max(4, words.size()), now);
            questions = QuizGenerator.forWords(words, repo.curriculum().allWords(), options);
        }
        return new QuizSession(questions, now);
    }

    private void resetQuestionState() {
        answered = false;
        chosenTokens.clear();
        poolTokens = new ArrayList<String>();
        input = null;
        Question question = session.current();
        if (question != null && question.type == QuestionType.SENTENCE_ORDER) {
            poolTokens.addAll(question.options);
        }
    }

    @Override
    public String title() {
        return titleText;
    }

    @Override
    public String subtitle() {
        if (session.total() == 0) {
            return null;
        }
        return "第 " + Math.min(session.position() + 1, session.total()) + " / " + session.total()
                + " 题  ·  快捷键：1–4 或 A–D 选择，Enter 提交 / 下一题";
    }

    @Override
    public void build(VBox content) {
        if (session.isEmpty()) {
            Card empty = Ui.card();
            empty.add(Ui.text("暂时没有可练习的内容", 17, Ui.TEXT, true));
            empty.add(Ui.spacer(6));
            empty.add(Ui.body("wrong".equals(mode)
                    ? "错题本是空的，先去做几次单元练习吧。"
                    : "今天没有到期需要复习的词，先在「知识引导」里学习新单元。"));
            empty.add(Ui.spacer(12));
            empty.add(Ui.align(Ui.primary("返回", new Runnable() {
                @Override
                public void run() {
                    nav.pop();
                }
            }), "left"));
            content.add(empty);
            return;
        }
        Question question = session.current();
        if (question == null) {
            return;
        }
        int percent = Math.round(session.position() * 100f / session.total());
        VBox progress = Ui.column(2);
        progress.add(Ui.progress(percent));
        progress.add(Ui.hint("已答对 " + session.correctCount() + " 题"));
        content.add(progress);

        Card card = Ui.card();
        card.add(Ui.align(tag(question.type.label), "left"));
        card.add(Ui.spacer(10));
        card.add(Ui.text(question.prompt, 21, Ui.TEXT, true));
        if (question.hint != null && question.hint.length() > 0) {
            card.add(Ui.spacer(6));
            card.add(Ui.hint(question.hint));
        }
        content.add(card);

        if (question.type.isChoice()) {
            content.add(choiceBlock(question));
        } else if (question.type == QuestionType.SPELLING) {
            content.add(spellingBlock());
        } else {
            content.add(orderBlock());
        }
        if (answered) {
            content.add(feedbackCard(question));
        }
    }

    private static JComponent tag(String label) {
        Card box = new Card(Ui.SOFT, null, 8, 4, 0);
        box.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        box.add(Ui.label(label, 12, Ui.PRIMARY, true));
        return box;
    }

    private JComponent choiceBlock(Question question) {
        VBox box = Ui.column(8);
        QuestionRecord record = session.lastRecord();
        for (int i = 0; i < question.options.size(); i++) {
            final int index = i;
            String letter = String.valueOf((char) ('A' + i));
            RoundButton option = Ui.outlined(letter + ".   " + question.options.get(i), Ui.TEXT, null);
            option.setHorizontalAlignment(SwingConstants.LEFT);
            option.setFont(Ui.font(16, false));
            option.setBorder(BorderFactory.createEmptyBorder(13, 18, 13, 18));
            if (answered && record != null) {
                if (index == question.correctIndex) {
                    option.setColors(Ui.GREEN, Ui.WHITE, null);
                } else if (record.answer != null && record.answer.choiceIndex == index) {
                    option.setColors(Ui.RED, Ui.WHITE, null);
                }
                option.setFocusable(false);
            } else {
                option.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        submit(Answer.choice(index));
                    }
                });
            }
            box.add(option);
        }
        return box;
    }

    private JComponent spellingBlock() {
        VBox box = Ui.column(10);
        input = new JTextField();
        input.setFont(Ui.font(22, false));
        input.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Ui.LINE, 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        QuestionRecord record = session.lastRecord();
        if (answered && record != null && record.answer != null) {
            input.setText(record.answer.text == null ? "" : record.answer.text);
            input.setEditable(false);
            input.setFocusable(false);
        } else {
            input.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    submitSpelling();
                }
            });
            nav.focusLater(input);
        }
        box.add(input);
        if (!answered) {
            box.add(Ui.primary("提交答案（Enter）", new Runnable() {
                @Override
                public void run() {
                    submitSpelling();
                }
            }));
        }
        return box;
    }

    private void submitSpelling() {
        if (!answered && input != null) {
            submit(Answer.text(input.getText()));
        }
    }

    private JComponent orderBlock() {
        VBox box = Ui.column(10);
        Card answerCard = Ui.card();
        answerCard.add(Ui.hint("你的句子"));
        answerCard.add(Ui.spacer(6));
        answerCard.add(Ui.text(chosenTokens.isEmpty() ? "（点击下方词块开始组句）" : join(chosenTokens),
                19, chosenTokens.isEmpty() ? Ui.SUB : Ui.TEXT, false));
        box.add(answerCard);
        if (!answered) {
            WrapBox pool = new WrapBox(8, 8);
            for (int i = 0; i < poolTokens.size(); i++) {
                final int index = i;
                RoundButton chip = Ui.soft(poolTokens.get(i), new Runnable() {
                    @Override
                    public void run() {
                        chosenTokens.add(poolTokens.remove(index));
                        nav.refresh();
                    }
                });
                chip.setFont(Ui.font(16, true));
                pool.add(chip);
            }
            box.add(pool);
            HBox actions = Ui.row(10);
            actions.add(Ui.weight(Ui.soft("撤销（Backspace）", new Runnable() {
                @Override
                public void run() {
                    undoToken();
                }
            }), 1f));
            actions.add(Ui.weight(Ui.primary("提交答案（Enter）", new Runnable() {
                @Override
                public void run() {
                    submitOrder();
                }
            }), 1f));
            box.add(actions);
        }
        return box;
    }

    private void undoToken() {
        if (!chosenTokens.isEmpty()) {
            poolTokens.add(chosenTokens.remove(chosenTokens.size() - 1));
            nav.refresh();
        }
    }

    private void submitOrder() {
        if (!answered) {
            submit(Answer.text(join(chosenTokens)));
        }
    }

    private JComponent feedbackCard(final Question question) {
        QuestionRecord record = session.lastRecord();
        boolean correct = record != null && record.correct;
        Card card = Ui.card(correct ? Ui.GREEN_BG : Ui.RED_BG);
        card.add(Ui.text(correct ? "✓ 回答正确" : "✗ 回答错误", 18, correct ? Ui.GREEN : Ui.RED, true));
        if (!correct) {
            card.add(Ui.spacer(6));
            card.add(Ui.text("正确答案：" + question.correctText, 16, Ui.TEXT, true));
            if (record != null && record.answer != null) {
                String mine = record.answer.display(question);
                if (mine.trim().length() > 0) {
                    card.add(Ui.hint("你的答案：" + mine));
                }
            }
        }
        card.add(Ui.divider());
        card.add(Ui.body(question.explanation));
        card.add(Ui.spacer(10));
        HBox actions = Ui.row(10);
        actions.add(Ui.weight(Ui.soft("▶ 朗读", new Runnable() {
            @Override
            public void run() {
                speakOrWarn(question.speakText);
            }
        }), 1f));
        boolean last = session.position() + 1 >= session.total();
        actions.add(Ui.weight(Ui.primary(last ? "查看成绩（Enter）" : "下一题（Enter）", new Runnable() {
            @Override
            public void run() {
                next();
            }
        }), 1f));
        card.add(actions);
        return card;
    }

    void submit(Answer answer) {
        session.submit(answer, System.currentTimeMillis());
        answered = true;
        nav.refresh();
    }

    void next() {
        if (!answered) {
            return;
        }
        if (session.next(System.currentTimeMillis())) {
            resetQuestionState();
            nav.refreshToTop();
        } else {
            finishQuiz();
        }
    }

    private void finishQuiz() {
        long now = System.currentTimeMillis();
        QuizResult result = session.result(now);
        String id = unit == null ? null : unit.id;
        repo.progress().recordQuiz(id, result, now);
        repo.lastResult = result;
        repo.lastQuizUnitId = id;
        repo.lastQuizTitle = titleText;
        nav.replace(new ResultPage(nav));
    }

    @Override
    public boolean handleKey(KeyEvent e) {
        Question question = session.current();
        if (question == null || e.isAltDown() || e.isControlDown()) {
            return false;
        }
        int code = e.getKeyCode();
        if (answered) {
            if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
                next();
                return true;
            }
            return false;
        }
        if (question.type.isChoice()) {
            int index = -1;
            if (code >= KeyEvent.VK_1 && code <= KeyEvent.VK_9) {
                index = code - KeyEvent.VK_1;
            } else if (code >= KeyEvent.VK_NUMPAD1 && code <= KeyEvent.VK_NUMPAD9) {
                index = code - KeyEvent.VK_NUMPAD1;
            } else if (code >= KeyEvent.VK_A && code <= KeyEvent.VK_Z) {
                index = code - KeyEvent.VK_A;
            }
            if (index >= 0 && index < question.options.size()) {
                submit(Answer.choice(index));
                return true;
            }
            return false;
        }
        if (question.type == QuestionType.SENTENCE_ORDER) {
            if (code == KeyEvent.VK_ENTER) {
                submitOrder();
                return true;
            }
            if (code == KeyEvent.VK_BACK_SPACE) {
                undoToken();
                return true;
            }
        }
        return false;
    }

    private static String join(List<String> tokens) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(tokens.get(i));
        }
        return sb.toString();
    }
}
