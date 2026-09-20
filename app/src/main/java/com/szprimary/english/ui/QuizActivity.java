package com.szprimary.english.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

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

import java.util.ArrayList;
import java.util.List;

/** 练习验证：出题、作答、即时判定与解析。 */
public final class QuizActivity extends Activity {

    private AppRepo repo;
    private QuizSession session;
    private Unit unit;
    private String mode;
    private String titleText = "练习验证";

    private boolean answered;
    private final List<String> chosenTokens = new ArrayList<String>();
    private List<String> poolTokens = new ArrayList<String>();
    private EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repo = AppRepo.get(this);
        getWindow().setStatusBarColor(Ui.PRIMARY_DARK);
        mode = getIntent().getStringExtra("mode");
        if (mode == null) {
            mode = "unit";
        }
        session = createSession();
        resetQuestionState();
        build();
    }

    private QuizSession createSession() {
        long now = System.currentTimeMillis();
        List<Question> questions;
        if ("unit".equals(mode)) {
            unit = repo.curriculum().unit(getIntent().getStringExtra("unitId"));
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

    private void build() {
        LinearLayout content = Ui.page(this, titleText,
                session.total() == 0 ? null : (session.position() + 1) + " / " + session.total());
        if (session.isEmpty()) {
            LinearLayout empty = Ui.card(this);
            empty.addView(Ui.text(this, "暂时没有可练习的内容", 17, Ui.TEXT, true));
            empty.addView(Ui.spacer(this, 6));
            empty.addView(Ui.body(this, "wrong".equals(mode)
                    ? "错题本是空的，先去做几次单元练习吧。"
                    : "今天没有到期需要复习的词，先在「知识引导」里学习新单元。"));
            Button back = Ui.primary(this, "返回");
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            empty.addView(back);
            content.addView(empty);
            return;
        }

        Question question = session.current();
        if (question == null) {
            finishQuiz();
            return;
        }

        int percent = Math.round(session.position() * 100f / session.total());
        content.addView(Ui.progress(this, percent));
        content.addView(Ui.spacer(this, 4));
        content.addView(Ui.hint(this, "已答对 " + session.correctCount() + " 题"));
        content.addView(Ui.spacer(this, 8));

        LinearLayout card = Ui.card(this);
        LinearLayout tagRow = Ui.row(this);
        tagRow.addView(tag(question.type.label));
        card.addView(tagRow);
        card.addView(Ui.spacer(this, 10));
        card.addView(Ui.text(this, question.prompt, 20, Ui.TEXT, true));
        if (question.hint != null && question.hint.length() > 0) {
            card.addView(Ui.spacer(this, 6));
            card.addView(Ui.hint(this, question.hint));
        }
        content.addView(card);

        if (question.type.isChoice()) {
            content.addView(choiceBlock(question));
        } else if (question.type == QuestionType.SPELLING) {
            content.addView(spellingBlock(question));
        } else {
            content.addView(orderBlock(question));
        }

        if (answered) {
            content.addView(feedbackCard(question));
        }
    }

    private View tag(String label) {
        LinearLayout box = Ui.column(this);
        box.setBackground(Ui.rounded(this, Ui.SOFT, 8));
        box.setPadding(Ui.dp(this, 10), Ui.dp(this, 4), Ui.dp(this, 10), Ui.dp(this, 4));
        box.addView(Ui.text(this, label, 12, Ui.PRIMARY, true));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        box.setLayoutParams(params);
        return box;
    }

    private View choiceBlock(final Question question) {
        LinearLayout box = Ui.column(this);
        QuestionRecord record = session.lastRecord();
        for (int i = 0; i < question.options.size(); i++) {
            final int index = i;
            String letter = String.valueOf((char) ('A' + i));
            Button option = Ui.button(this, letter + ".  " + question.options.get(i), Ui.CARD, Ui.TEXT);
            option.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
            option.setBackground(Ui.outlined(this, Ui.LINE, 12));
            if (answered && record != null) {
                if (index == question.correctIndex) {
                    option.setBackground(Ui.rounded(this, Ui.GREEN, 12));
                    option.setTextColor(0xFFFFFFFF);
                } else if (record.answer != null && record.answer.choiceIndex == index) {
                    option.setBackground(Ui.rounded(this, Ui.RED, 12));
                    option.setTextColor(0xFFFFFFFF);
                }
            } else {
                option.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        submit(Answer.choice(index));
                    }
                });
            }
            box.addView(option);
        }
        return box;
    }

    private View spellingBlock(final Question question) {
        LinearLayout box = Ui.column(this);
        input = new EditText(this);
        input.setHint("在这里输入英文单词");
        input.setTextSize(20);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setBackground(Ui.outlined(this, Ui.LINE, 12));
        input.setPadding(Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14));
        QuestionRecord record = session.lastRecord();
        if (answered && record != null && record.answer != null) {
            input.setText(record.answer.text == null ? "" : record.answer.text);
            input.setEnabled(false);
        }
        box.addView(input);

        if (!answered) {
            Button submit = Ui.primary(this, "提交答案");
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    submit(Answer.text(input.getText().toString()));
                }
            });
            box.addView(submit);
        }
        return box;
    }

    private View orderBlock(final Question question) {
        LinearLayout box = Ui.column(this);

        LinearLayout answerCard = Ui.card(this);
        answerCard.addView(Ui.hint(this, "你的句子"));
        answerCard.addView(Ui.spacer(this, 6));
        answerCard.addView(Ui.text(this, chosenTokens.isEmpty() ? "（点击下方词块开始组句）" : join(chosenTokens),
                18, chosenTokens.isEmpty() ? Ui.SUB : Ui.TEXT, false));
        box.addView(answerCard);

        if (!answered) {
            LinearLayout pool = Ui.column(this);
            LinearLayout line = Ui.row(this);
            int perLine = 0;
            for (int i = 0; i < poolTokens.size(); i++) {
                final int index = i;
                Button chip = Ui.button(this, poolTokens.get(i), Ui.SOFT, Ui.PRIMARY);
                chip.setTextSize(15);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.rightMargin = Ui.dp(this, 6);
                params.topMargin = Ui.dp(this, 6);
                chip.setLayoutParams(params);
                chip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        chosenTokens.add(poolTokens.remove(index));
                        build();
                    }
                });
                line.addView(chip);
                perLine++;
                if (perLine == 3) {
                    pool.addView(line);
                    line = Ui.row(this);
                    perLine = 0;
                }
            }
            if (perLine > 0) {
                pool.addView(line);
            }
            box.addView(pool);

            LinearLayout actions = Ui.row(this);
            Button undo = Ui.soft(this, "撤销");
            LinearLayout.LayoutParams up = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            up.rightMargin = Ui.dp(this, 5);
            up.topMargin = Ui.dp(this, 10);
            undo.setLayoutParams(up);
            undo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!chosenTokens.isEmpty()) {
                        poolTokens.add(chosenTokens.remove(chosenTokens.size() - 1));
                        build();
                    }
                }
            });
            actions.addView(undo);

            Button submit = Ui.primary(this, "提交答案");
            LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            sp.leftMargin = Ui.dp(this, 5);
            sp.topMargin = Ui.dp(this, 10);
            submit.setLayoutParams(sp);
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    submit(Answer.text(join(chosenTokens)));
                }
            });
            actions.addView(submit);
            box.addView(actions);
        }
        return box;
    }

    private View feedbackCard(final Question question) {
        QuestionRecord record = session.lastRecord();
        boolean correct = record != null && record.correct;

        LinearLayout card = Ui.card(this);
        card.setBackground(Ui.rounded(this, correct ? 0xFFE9F7F1 : 0xFFFDECEC, 14));
        card.addView(Ui.text(this, correct ? "✓ 回答正确" : "✗ 回答错误", 18,
                correct ? Ui.GREEN : Ui.RED, true));
        if (!correct) {
            card.addView(Ui.spacer(this, 6));
            card.addView(Ui.text(this, "正确答案：" + question.correctText, 16, Ui.TEXT, true));
            if (record != null && record.answer != null) {
                String mine = record.answer.display(question);
                if (mine.trim().length() > 0) {
                    card.addView(Ui.hint(this, "你的答案：" + mine));
                }
            }
        }
        card.addView(Ui.divider(this));
        card.addView(Ui.body(this, question.explanation));

        LinearLayout actions = Ui.row(this);
        Button speak = Ui.soft(this, "🔊 朗读");
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        sp.rightMargin = Ui.dp(this, 5);
        sp.topMargin = Ui.dp(this, 10);
        speak.setLayoutParams(sp);
        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (repo.speaker().isReady()) {
                    repo.speaker().speak(question.speakText);
                } else {
                    Ui.toast(QuizActivity.this, "本机暂无英语语音引擎");
                }
            }
        });
        actions.addView(speak);

        boolean last = session.position() + 1 >= session.total();
        Button next = Ui.primary(this, last ? "查看成绩" : "下一题");
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        np.leftMargin = Ui.dp(this, 5);
        np.topMargin = Ui.dp(this, 10);
        next.setLayoutParams(np);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (session.next(System.currentTimeMillis())) {
                    resetQuestionState();
                    build();
                } else {
                    finishQuiz();
                }
            }
        });
        actions.addView(next);
        card.addView(actions);
        return card;
    }

    private void submit(Answer answer) {
        session.submit(answer, System.currentTimeMillis());
        answered = true;
        build();
    }

    private void finishQuiz() {
        QuizResult result = session.result(System.currentTimeMillis());
        String unitId = unit == null ? null : unit.id;
        repo.progress().recordQuiz(unitId, result, System.currentTimeMillis());
        repo.lastResult = result;
        repo.lastQuizUnitId = unitId;
        repo.lastQuizTitle = titleText;
        startActivity(new Intent(this, ResultActivity.class));
        finish();
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
