package com.szprimary.english.desktop.pages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.szprimary.english.core.model.Unit;
import com.szprimary.english.core.model.Word;
import com.szprimary.english.core.progress.MemoryProgressStore;
import com.szprimary.english.core.quiz.Question;
import com.szprimary.english.core.quiz.QuestionType;
import com.szprimary.english.desktop.DesktopRepo;
import com.szprimary.english.desktop.ResourceContentSource;
import com.szprimary.english.desktop.Speaker;
import com.szprimary.english.desktop.ui.Navigator;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.junit.Test;

/** 在无显示器环境里构建并操作全部页面，覆盖界面与核心层的衔接。 */
public class PagesTest {

    private interface Body {
        void run() throws Exception;
    }

    /** Swing 组件只在事件线程里操作。 */
    private static void onEdt(final Body body) throws Exception {
        final Exception[] error = new Exception[1];
        final Error[] fatal = new Error[1];
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    body.run();
                } catch (Exception e) {
                    error[0] = e;
                } catch (Error e) {
                    fatal[0] = e;
                }
            }
        });
        if (fatal[0] != null) {
            throw fatal[0];
        }
        if (error[0] != null) {
            throw error[0];
        }
    }

    private static DesktopRepo repo() {
        return new DesktopRepo(new ResourceContentSource(), new MemoryProgressStore(), "内存", new Speaker.Silent("测试环境无语音"));
    }

    private static Navigator nav(DesktopRepo repo) {
        Navigator nav = new Navigator(repo);
        nav.setSize(1000, 760);
        nav.push(new HomePage(nav));
        layout(nav);
        return nav;
    }

    private static void layout(Component c) {
        if (c instanceof Container) {
            ((Container) c).doLayout();
            for (Component child : ((Container) c).getComponents()) {
                layout(child);
            }
        }
    }

    private static List<Component> all(Component root) {
        List<Component> out = new ArrayList<Component>();
        collect(root, out);
        return out;
    }

    private static void collect(Component c, List<Component> out) {
        out.add(c);
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                collect(child, out);
            }
        }
    }

    private static String text(Navigator nav) {
        StringBuilder sb = new StringBuilder(nav.titleText()).append('\n');
        for (Component c : all(nav.pageContent())) {
            if (c instanceof JTextComponent) {
                sb.append(((JTextComponent) c).getText()).append('\n');
            } else if (c instanceof JLabel) {
                sb.append(((JLabel) c).getText()).append('\n');
            } else if (c instanceof AbstractButton) {
                sb.append(((AbstractButton) c).getText()).append('\n');
            }
        }
        return sb.toString();
    }

    private static AbstractButton button(Navigator nav, String prefix) {
        for (Component c : all(nav.pageContent())) {
            if (c instanceof AbstractButton && c.isVisible() && ((AbstractButton) c).getText().startsWith(prefix)) {
                return (AbstractButton) c;
            }
        }
        fail("找不到按钮「" + prefix + "」，页面内容：\n" + text(nav));
        return null;
    }

    private static AbstractButton exactButton(Navigator nav, String label) {
        for (Component c : all(nav.pageContent())) {
            if (c instanceof AbstractButton && c.isVisible() && ((AbstractButton) c).getText().equals(label)) {
                return (AbstractButton) c;
            }
        }
        fail("找不到词块「" + label + "」");
        return null;
    }

    private static void click(Navigator nav, String prefix) {
        button(nav, prefix).doClick();
        layout(nav);
    }

    private static KeyEvent key(Navigator nav, int code) {
        return new KeyEvent(nav, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, code, KeyEvent.CHAR_UNDEFINED);
    }

    /** 通过界面上的按钮回答当前题，然后进入下一题。 */
    private static void answer(Navigator nav, QuizPage page, boolean correct) {
        Question q = page.session().current();
        assertNotNull(q);
        if (q.type.isChoice()) {
            int index = correct ? q.correctIndex : (q.correctIndex + 1) % q.options.size();
            click(nav, (char) ('A' + index) + ".");
        } else if (q.type == QuestionType.SPELLING) {
            JTextField field = null;
            for (Component c : all(nav.pageContent())) {
                if (c instanceof JTextField) {
                    field = (JTextField) c;
                }
            }
            assertNotNull("拼写题应有输入框", field);
            field.setText(correct ? q.correctText : "zzz");
            click(nav, "提交答案");
        } else {
            if (correct) {
                for (String token : q.correctText.split(" ")) {
                    exactButton(nav, token).doClick();
                    layout(nav);
                }
            }
            click(nav, "提交答案");
        }
        String feedback = text(nav);
        assertTrue(q.prompt + " 判定不符：\n" + feedback, feedback.contains(correct ? "✓ 回答正确" : "✗ 回答错误"));
        boolean last = page.session().position() + 1 >= page.session().total();
        click(nav, last ? "查看成绩" : "下一题");
    }

    @Test
    public void homeShowsAllGradesAndLaysOut() throws Exception {
        onEdt(new Body() {
            @Override
            public void run() {
                Navigator nav = nav(repo());
                String t = text(nav);
                for (String g : new String[]{"一年级", "二年级", "三年级", "四年级", "五年级", "六年级"}) {
                    assertTrue(g, t.contains(g));
                }
                assertTrue(t.contains("学习概览"));
                assertTrue("页面应有实际高度", nav.pageContent().getHeight() > 400);
            }
        });
    }

    @Test
    public void everyGradeUnitsAndLearnSectionsBuild() throws Exception {
        onEdt(new Body() {
            @Override
            public void run() {
                DesktopRepo repo = repo();
                Navigator nav = nav(repo);
                for (int g = 1; g <= 6; g++) {
                    nav.push(new UnitsPage(nav, g));
                    layout(nav);
                    for (Unit unit : repo.curriculum().grade(g).units) {
                        assertTrue(text(nav).contains(unit.displayTitle()));
                        nav.push(new LearnPage(nav, unit.id));
                        layout(nav);
                        assertTrue(text(nav).contains(unit.words.get(0).en));
                        click(nav, "句型");
                        assertTrue(text(nav).contains(unit.patterns.get(0).en));
                        click(nav, "语法");
                        assertTrue(text(nav).contains(unit.grammar.get(0).title));
                        assertTrue(unit.id + " 应显示教材对照", text(nav).contains("教材对照"));
                        nav.pop();
                    }
                    nav.pop();
                }
                assertEquals(1, nav.depth());
            }
        });
    }

    @Test
    public void learnPageCardsKeysAndMarking() throws Exception {
        onEdt(new Body() {
            @Override
            public void run() {
                DesktopRepo repo = repo();
                Navigator nav = nav(repo);
                Unit unit = repo.curriculum().unit("g3u7");
                Word first = unit.words.get(0);
                nav.push(new LearnPage(nav, unit.id));
                layout(nav);
                assertTrue(text(nav).contains("显示中文释义与例句"));
                assertFalse(text(nav).contains(first.exEn));
                assertTrue(nav.onKey(key(nav, KeyEvent.VK_SPACE)));
                assertTrue("空格应显示例句", text(nav).contains(first.exEn));
                assertTrue(text(nav).contains("▶ 朗读例句"));
                click(nav, "标记已掌握");
                assertTrue(repo.isLearned(first));
                assertTrue(text(nav).contains("✓ 已掌握"));
                assertTrue(nav.onKey(key(nav, KeyEvent.VK_RIGHT)));
                assertTrue(text(nav).contains("第 2 / " + unit.words.size() + " 个词"));
                assertTrue(nav.onKey(key(nav, KeyEvent.VK_M)));
                assertTrue(repo.isLearned(unit.words.get(1)));
                assertTrue(nav.onKey(key(nav, KeyEvent.VK_ESCAPE)));
                assertEquals(1, nav.depth());
                assertEquals(2, repo.progress().learnedWordCount());
                assertTrue("首页应显示新的已学词数", text(nav).contains("2 / " + repo.curriculum().totalWords()));
            }
        });
    }

    @Test
    public void speakingWithoutEngineShowsHint() throws Exception {
        onEdt(new Body() {
            @Override
            public void run() {
                Navigator nav = nav(repo());
                nav.push(new LearnPage(nav, "g1u1"));
                layout(nav);
                click(nav, "▶ 朗读");
                assertNotNull(nav.lastToast());
                assertTrue(nav.lastToast().contains("语音"));
            }
        });
    }

    @Test
    public void everyUnitQuizCanBeCompletedThroughTheUi() throws Exception {
        onEdt(new Body() {
            @Override
            public void run() {
                DesktopRepo repo = repo();
                for (Unit unit : repo.curriculum().allUnits()) {
                    Navigator nav = nav(repo);
                    QuizPage quiz = QuizPage.forUnit(nav, unit.id);
                    nav.push(quiz);
                    layout(nav);
                    int total = quiz.session().total();
                    assertTrue(unit.id + " 应有题目", total > 0);
                    for (int i = 0; i < total; i++) {
                        answer(nav, quiz, true);
                    }
                    assertTrue(unit.id + " 答完应进入成绩页", nav.current() instanceof ResultPage);
                    assertEquals(unit.id, 100, repo.lastResult.score);
                    assertTrue(text(nav).contains("全部答对"));
                    assertEquals(100, repo.progress().unit(unit.id).bestScore);
                }
                assertTrue(repo.progress().totalAnswered() > 400);
            }
        });
    }

    @Test
    public void wrongAnswersFlowIntoWrongBookAndReview() throws Exception {
        onEdt(new Body() {
            @Override
            public void run() {
                DesktopRepo repo = repo();
                Navigator nav = nav(repo);
                QuizPage quiz = QuizPage.forUnit(nav, "g4u7");
                nav.push(quiz);
                layout(nav);
                int total = quiz.session().total();
                for (int i = 0; i < total; i++) {
                    answer(nav, quiz, false);
                }
                assertEquals(0, repo.lastResult.score);
                String report = text(nav);
                assertTrue(report.contains("错题回顾"));
                assertFalse(repo.progress().wrongBook().isEmpty());

                click(nav, "只练错题");
                assertEquals("错题本练习", nav.titleText());
                assertTrue(nav.onKey(key(nav, KeyEvent.VK_ESCAPE)));

                nav.push(new ReviewPage(nav, "wrong"));
                layout(nav);
                assertTrue(text(nav).contains("开始错题练习"));
                click(nav, "开始错题练习");
                assertEquals("错题本练习", nav.titleText());
                QuizPage wrongQuiz = (QuizPage) nav.current();
                assertTrue(wrongQuiz.session().total() > 0);
                // 键盘作答：按数字键选择
                Question q = wrongQuiz.session().current();
                if (q.type.isChoice()) {
                    assertTrue(nav.onKey(key(nav, KeyEvent.VK_1 + q.correctIndex)));
                    assertTrue(text(nav).contains("✓ 回答正确"));
                    assertTrue(nav.onKey(key(nav, KeyEvent.VK_ENTER)));
                }
            }
        });
    }

    @Test
    public void emptyReviewShowsMessage() throws Exception {
        onEdt(new Body() {
            @Override
            public void run() {
                Navigator nav = nav(repo());
                nav.push(QuizPage.forReview(nav, "wrong"));
                layout(nav);
                assertTrue(text(nav).contains("错题本是空的"));
                click(nav, "返回");
                assertEquals(1, nav.depth());
                nav.push(new ReviewPage(nav, "due"));
                layout(nav);
                assertTrue(text(nav).contains("今天没有到期的复习内容"));
            }
        });
    }

    @Test
    public void statsAndAboutBuild() throws Exception {
        onEdt(new Body() {
            @Override
            public void run() {
                Navigator nav = nav(repo());
                nav.push(new StatsPage(nav));
                layout(nav);
                assertTrue(text(nav).contains("总体情况"));
                assertTrue(text(nav).contains("未练习"));
                nav.push(new AboutPage(nav));
                layout(nav);
                String t = text(nav);
                assertTrue(t.contains("版本 " + DesktopRepo.version()));
                assertTrue(t.contains("测试环境无语音"));
                assertTrue(t.contains("沪教牛津版（深圳用）"));
            }
        });
    }
}
