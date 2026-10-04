package com.szprimary.english.desktop.pages;

import com.szprimary.english.core.model.Unit;
import com.szprimary.english.core.quiz.Answer;
import com.szprimary.english.core.quiz.Question;
import com.szprimary.english.core.quiz.QuestionType;
import com.szprimary.english.desktop.DesktopRepo;
import com.szprimary.english.desktop.FileProgressStore;
import com.szprimary.english.desktop.ResourceContentSource;
import com.szprimary.english.desktop.Speaker;
import com.szprimary.english.desktop.ui.AppFrame;
import com.szprimary.english.desktop.ui.Navigator;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.SwingUtilities;

/** 开发用：在 Xvfb 下打开真实窗口，逐页截图检查排版（不随程序发布）。 */
public final class ScreenshotTool {

    private static File out;
    private static AppFrame frame;

    public static void main(String[] args) throws Exception {
        out = new File(args.length > 0 ? args[0] : "screenshots");
        out.mkdirs();
        final int width = args.length > 1 ? Integer.parseInt(args[1]) : 1040;
        final File data = Files.createTempDirectory("szshots").toFile();
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                DesktopRepo repo = new DesktopRepo(new ResourceContentSource(),
                        new FileProgressStore(new File(data, "progress.json")),
                        new File(data, "progress.json").getAbsolutePath(),
                        new Speaker.Silent("使用英语语音「Microsoft Zira Desktop」朗读。（截图演示）"));
                seed(repo);
                com.szprimary.english.desktop.Main.lookAndFeel();
                frame = new AppFrame(repo);
                frame.setSize(width, 800);
                frame.nav().push(new HomePage(frame.nav()));
                frame.setVisible(true);
            }
        });
        String suffix = width == 1040 ? "" : "-" + width;
        shot("01-home" + suffix);
        if (width != 1040) {
            onEdt(() -> nav().push(new LearnPage(nav(), "g5u7")));
            shot("02-learn-narrow" + suffix);
            System.exit(0);
        }
        onEdt(() -> nav().push(new UnitsPage(nav(), 3)));
        shot("02-units-grade3");
        onEdt(() -> {
            nav().push(new LearnPage(nav(), "g3u7"));
            nav().onKey(key(KeyEvent.VK_SPACE));
        });
        shot("03-learn-wordcard");
        onEdt(() -> click("语法"));
        shot("04-learn-grammar-tips");
        onEdt(() -> click("句型"));
        shot("05-learn-patterns");

        onEdt(() -> {
            nav().pop();
            nav().push(QuizPage.forUnit(nav(), "g3u7"));
            advanceTo(QuestionType.CN_TO_EN, QuestionType.EN_TO_CN);
        });
        shot("06-quiz-choice");
        onEdt(() -> {
            QuizPage q = (QuizPage) nav().current();
            Question question = q.session().current();
            click((char) ('A' + (question.correctIndex + 1) % question.options.size()) + ".");
        });
        shot("07-quiz-wrong-feedback");
        onEdt(() -> {
            click("下一题");
            advanceTo(QuestionType.SENTENCE_ORDER);
            QuizPage q = (QuizPage) nav().current();
            if (q.session().current() != null && q.session().current().type == QuestionType.SENTENCE_ORDER) {
                String[] tokens = q.session().current().correctText.split(" ");
                exact(tokens[0]).doClick();
                if (tokens.length > 2) {
                    exact(tokens[1]).doClick();
                }
            }
        });
        shot("08-quiz-sentence-order");
        onEdt(() -> {
            nav().replace(QuizPage.forUnit(nav(), "g4u7"));
            advanceTo(QuestionType.SPELLING);
        });
        shot("09-quiz-spelling");
        onEdt(() -> {
            QuizPage q = (QuizPage) nav().current();
            boolean wrong = true;
            while (q.session().current() != null && nav().current() == q) {
                Question question = q.session().current();
                q.submit(wrong ? Answer.text("zzz") : correct(question));
                wrong = !wrong;
                q.next();
            }
        });
        shot("10-result");
        onEdt(() -> {
            nav().pop();
            nav().push(new AboutPage(nav()));
        });
        shot("11-about");
        onEdt(() -> {
            nav().pop();
            nav().push(new StatsPage(nav()));
        });
        shot("12-stats");
        System.exit(0);
    }

    private interface Body {
        void run();
    }

    private static void onEdt(final Body body) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                body.run();
            }
        });
    }

    private static Navigator nav() {
        return frame.nav();
    }

    private static KeyEvent key(int code) {
        return new KeyEvent(nav(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, code, KeyEvent.CHAR_UNDEFINED);
    }

    private static Answer correct(Question q) {
        return q.type.isChoice() ? Answer.choice(q.correctIndex) : Answer.text(q.correctText);
    }

    /** 答对前面的题，直到出现指定题型。 */
    private static void advanceTo(QuestionType... types) {
        QuizPage q = (QuizPage) nav().current();
        for (int guard = 0; guard < 40; guard++) {
            Question question = q.session().current();
            if (question == null) {
                return;
            }
            for (QuestionType t : types) {
                if (question.type == t) {
                    nav().refreshToTop();
                    return;
                }
            }
            q.submit(correct(question));
            q.next();
        }
    }

    private static void click(String prefix) {
        find(nav().pageContent(), prefix, false).doClick();
    }

    private static AbstractButton exact(String label) {
        return find(nav().pageContent(), label, true);
    }

    private static AbstractButton find(Component c, String text, boolean exact) {
        if (c instanceof AbstractButton && c.isVisible()) {
            String t = ((AbstractButton) c).getText();
            if (exact ? t.equals(text) : t.startsWith(text)) {
                return (AbstractButton) c;
            }
        }
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                AbstractButton b = find(child, text, exact);
                if (b != null) {
                    return b;
                }
            }
        }
        if (c == nav().pageContent()) {
            throw new IllegalStateException("找不到按钮：" + text);
        }
        return null;
    }

    private static void seed(DesktopRepo repo) {
        long now = System.currentTimeMillis();
        int n = 0;
        for (Unit unit : repo.curriculum().allUnits()) {
            if (unit.grade > 4) {
                break;
            }
            int take = unit.grade == 1 ? unit.words.size() : unit.grade == 2 ? 6 : 3;
            for (int i = 0; i < Math.min(take, unit.words.size()); i++) {
                repo.progress().markLearned(unit.id, unit.words.get(i).key(), now);
                n++;
            }
        }
        for (int i = 0; i < 20; i++) {
            Unit unit = repo.curriculum().allUnits().get(i);
            repo.progress().recordAnswer(unit.words.get(0).key(), i % 4 != 0, now - 86400000L * 3);
        }
        repo.progress().save();
    }

    private static void shot(String name) throws Exception {
        Thread.sleep(400);
        final BufferedImage[] img = new BufferedImage[1];
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                Component root = frame.getRootPane();
                BufferedImage image = new BufferedImage(root.getWidth(), root.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g = image.createGraphics();
                root.paint(g);
                g.dispose();
                img[0] = image;
            }
        });
        ImageIO.write(img[0], "png", new File(out, name + ".png"));
        System.out.println("saved " + name);
    }
}
