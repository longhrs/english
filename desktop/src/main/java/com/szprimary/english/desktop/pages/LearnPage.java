package com.szprimary.english.desktop.pages;

import com.szprimary.english.core.model.Example;
import com.szprimary.english.core.model.GrammarNote;
import com.szprimary.english.core.model.SentencePattern;
import com.szprimary.english.core.model.Unit;
import com.szprimary.english.core.model.Word;
import com.szprimary.english.desktop.ui.Navigator;
import com.szprimary.english.desktop.ui.Card;
import com.szprimary.english.desktop.ui.HBox;
import com.szprimary.english.desktop.ui.ImageBox;
import com.szprimary.english.desktop.ui.RoundButton;
import com.szprimary.english.desktop.ui.Ui;
import com.szprimary.english.desktop.ui.VBox;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import javax.swing.JComponent;

/** 知识引导：词卡、词表、句型、语法和学习小贴士。 */
public final class LearnPage extends BasePage {

    private final Unit unit;
    private int section;
    private int wordIndex;
    private boolean revealed;

    public LearnPage(Navigator nav, String unitId) {
        super(nav);
        this.unit = repo.curriculum().unit(unitId);
    }

    @Override
    public String title() {
        return unit == null ? "知识引导" : unit.displayTitle();
    }

    @Override
    public String subtitle() {
        return unit == null ? null : unit.titleCn + "  ·  快捷键：← → 切换单词，空格显示释义，M 标记掌握，S 朗读";
    }

    @Override
    public void build(VBox content) {
        if (unit == null) {
            content.add(Ui.body("没有找到该单元。"));
            return;
        }
        content.add(switcher());
        if (section == 0) {
            content.add(overviewCard());
            content.add(wordCard());
            content.add(wordListCard());
        } else if (section == 1) {
            for (int i = 0; i < unit.patterns.size(); i++) {
                content.add(patternCard(unit.patterns.get(i)));
            }
        } else if (section == 3) {
            buildImages(content);
        } else {
            for (int i = 0; i < unit.grammar.size(); i++) {
                content.add(grammarCard(unit.grammar.get(i)));
            }
            if (!unit.tips.isEmpty()) {
                Card tips = Ui.card();
                tips.add(Ui.text("学习小贴士", 16, Ui.ACCENT, true));
                for (int i = 0; i < unit.tips.size(); i++) {
                    tips.add(Ui.spacer(6));
                    tips.add(Ui.body("· " + unit.tips.get(i)));
                }
                content.add(tips);
            }
        }
        content.add(Ui.primary("开始本单元练习", new Runnable() {
            @Override
            public void run() {
                nav.push(QuizPage.forUnit(nav, unit.id));
            }
        }));
    }

    private JComponent switcher() {
        HBox row = Ui.row(8);
        row.add(tab("词汇 " + unit.words.size(), 0));
        row.add(tab("句型 " + unit.patterns.size(), 1));
        row.add(tab("语法 " + unit.grammar.size(), 2));
        if (repo.images().enabled()) {
            row.add(tab("课本图片 " + repo.images().count(unit.id), 3));
        }
        return row;
    }

    private JComponent tab(String label, final int index) {
        boolean active = section == index;
        Runnable action = new Runnable() {
            @Override
            public void run() {
                section = index;
                revealed = false;
                nav.refreshToTop();
            }
        };
        RoundButton button = active ? Ui.primary(label, action) : Ui.outlined(label, Ui.SUB, action);
        return Ui.weight(button, 1f);
    }

    private JComponent overviewCard() {
        Card card = Ui.card();
        card.add(Ui.text("本单元学什么", 16, Ui.TEXT, true));
        card.add(Ui.spacer(6));
        card.add(Ui.body(unit.overview));
        if (unit.phonics != null) {
            card.add(Ui.divider());
            card.add(Ui.text("语音重点：" + unit.phonics.focus, 15, Ui.PRIMARY, true));
            card.add(Ui.spacer(4));
            card.add(Ui.body(unit.phonics.tip));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < unit.phonics.examples.size(); i++) {
                if (i > 0) {
                    sb.append("    ");
                }
                sb.append(unit.phonics.examples.get(i));
            }
            card.add(Ui.spacer(4));
            card.add(Ui.hint(sb.toString()));
        }
        return card;
    }

    private Word currentWord() {
        if (unit.words.isEmpty()) {
            return null;
        }
        if (wordIndex >= unit.words.size()) {
            wordIndex = 0;
        }
        return unit.words.get(wordIndex);
    }

    private JComponent wordCard() {
        final Word word = currentWord();
        if (word == null) {
            return Ui.card();
        }
        final boolean learned = repo.isLearned(word);
        Card card = Ui.card();
        HBox top = Ui.row(10);
        VBox left = Ui.column(2);
        left.add(Ui.text(word.en, 32, Ui.TEXT, true));
        left.add(Ui.text(word.ipa + "   " + word.pos, 16, Ui.SUB, false));
        top.add(Ui.weight(left, 1f));
        VBox speakBox = Ui.column(0);
        speakBox.add(Ui.soft("▶ 朗读", new Runnable() {
            @Override
            public void run() {
                speakOrWarn(word.en);
            }
        }));
        top.add(speakBox);
        card.add(top);
        card.add(Ui.spacer(12));
        if (revealed) {
            card.add(Ui.text(word.cn, 21, Ui.PRIMARY, true));
            card.add(Ui.spacer(8));
            card.add(Ui.body(word.exEn));
            card.add(Ui.hint(word.exCn));
            card.add(Ui.spacer(8));
            card.add(Ui.align(Ui.soft("▶ 朗读例句", new Runnable() {
                @Override
                public void run() {
                    speakOrWarn(word.exEn);
                }
            }), "left"));
        } else {
            card.add(Ui.align(Ui.soft("显示中文释义与例句（空格）", new Runnable() {
                @Override
                public void run() {
                    reveal();
                }
            }), "left"));
        }
        card.add(Ui.divider());
        card.add(Ui.hint("第 " + (wordIndex + 1) + " / " + unit.words.size() + " 个词"));
        card.add(Ui.spacer(10));
        HBox nav = Ui.row(8);
        nav.add(Ui.weight(Ui.soft("← 上一个", new Runnable() {
            @Override
            public void run() {
                move(-1);
            }
        }), 1f));
        Runnable toggle = new Runnable() {
            @Override
            public void run() {
                toggleLearned();
            }
        };
        nav.add(Ui.weight(learned ? Ui.button("✓ 已掌握", Ui.GREEN, Ui.WHITE, toggle)
                : Ui.soft("标记已掌握", toggle), 1f));
        nav.add(Ui.weight(Ui.soft("下一个 →", new Runnable() {
            @Override
            public void run() {
                move(1);
            }
        }), 1f));
        card.add(nav);
        return card;
    }

    private void move(int delta) {
        if (unit.words.isEmpty()) {
            return;
        }
        wordIndex = (wordIndex + delta + unit.words.size()) % unit.words.size();
        revealed = false;
        nav.refresh();
    }

    private void reveal() {
        revealed = true;
        nav.refresh();
    }

    private void toggleLearned() {
        Word word = currentWord();
        if (word == null) {
            return;
        }
        if (repo.isLearned(word)) {
            repo.progress().unmarkLearned(unit.id, word.key());
        } else {
            repo.progress().markLearned(unit.id, word.key(), System.currentTimeMillis());
        }
        repo.progress().save();
        nav.refresh();
    }

    private JComponent wordListCard() {
        Card card = Ui.card();
        card.add(Ui.text("本单元词表", 16, Ui.TEXT, true));
        card.add(Ui.spacer(4));
        for (int i = 0; i < unit.words.size(); i++) {
            final int index = i;
            Word word = unit.words.get(i);
            Card row = new Card(Ui.CARD, null, 8, 2, 0);
            row.add(wordRow(word.en + "  " + word.ipa, word.pos + " " + word.cn,
                    repo.isLearned(word) ? "✓" : "", Ui.GREEN, index == wordIndex));
            Ui.clickable(row, new Runnable() {
                @Override
                public void run() {
                    wordIndex = index;
                    revealed = true;
                    section = 0;
                    nav.refresh();
                }
            });
            card.add(row);
            if (i < unit.words.size() - 1) {
                card.add(thinLine());
            }
        }
        return card;
    }

    private static JComponent thinLine() {
        JComponent line = new JComponent() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                g.setColor(Ui.LINE);
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        line.setPreferredSize(new java.awt.Dimension(1, 1));
        return line;
    }

    private JComponent patternCard(SentencePattern pattern) {
        Card card = Ui.card();
        card.add(Ui.text(pattern.en, 18, Ui.TEXT, true));
        card.add(Ui.spacer(4));
        card.add(Ui.text(pattern.cn, 15, Ui.PRIMARY, false));
        card.add(Ui.divider());
        for (int i = 0; i < pattern.examples.size(); i++) {
            final Example example = pattern.examples.get(i);
            HBox row = Ui.row(10);
            VBox texts = Ui.column(2);
            texts.add(Ui.body(example.en));
            texts.add(Ui.hint(example.cn));
            row.add(Ui.weight(texts, 1f));
            VBox speak = Ui.column(0);
            speak.add(Ui.soft("▶", new Runnable() {
                @Override
                public void run() {
                    speakOrWarn(example.en);
                }
            }));
            row.add(speak);
            card.add(row);
            if (i < pattern.examples.size() - 1) {
                card.add(Ui.spacer(8));
            }
        }
        return card;
    }

    private JComponent grammarCard(GrammarNote note) {
        Card card = Ui.card();
        card.add(Ui.text(note.title, 17, Ui.TEXT, true));
        card.add(Ui.spacer(6));
        card.add(Ui.body(note.explain));
        if (!note.examples.isEmpty()) {
            card.add(Ui.divider());
            for (int i = 0; i < note.examples.size(); i++) {
                final Example example = note.examples.get(i);
                HBox row = Ui.row(10);
                VBox texts = Ui.column(2);
                texts.add(Ui.body("· " + example.en));
                texts.add(Ui.hint("   " + example.cn));
                row.add(Ui.weight(texts, 1f));
                VBox speak = Ui.column(0);
                speak.add(Ui.soft("▶", new Runnable() {
                    @Override
                    public void run() {
                        speakOrWarn(example.en);
                    }
                }));
                row.add(speak);
                card.add(row);
                if (i < note.examples.size() - 1) {
                    card.add(Ui.spacer(6));
                }
            }
        }
        return card;
    }

    // ---- 课本图片：家长自己拍照或截图添加，只保存在本机 ----

    private void buildImages(VBox content) {
        List<File> files = repo.images().list(unit.id);
        Card intro = Ui.card();
        intro.add(Ui.text("课本图片", 16, Ui.TEXT, true));
        intro.add(Ui.spacer(4));
        intro.add(Ui.body("把课本里这一单元的页面拍照或截图后添加进来，学习时可以对照课本的插图和对话。"
                + "图片只保存在这台电脑上，不会上传，也不会打包进程序。"));
        intro.add(Ui.spacer(10));
        intro.add(Ui.align(Ui.primary("＋ 添加课本图片…", new Runnable() {
            @Override
            public void run() {
                chooseImages();
            }
        }), "left"));
        content.add(intro);
        if (files.isEmpty()) {
            Card empty = Ui.card();
            empty.add(Ui.hint("还没有添加图片。支持 JPG、PNG、GIF、BMP，一次可以选多张。"));
            content.add(empty);
            return;
        }
        for (int i = 0; i < files.size(); i++) {
            final File file = files.get(i);
            final BufferedImage img = repo.images().load(file);
            Card card = Ui.card();
            card.add(Ui.hint("第 " + (i + 1) + " / " + files.size() + " 张"));
            card.add(Ui.spacer(6));
            if (img == null) {
                card.add(Ui.body("这张图片无法读取：" + file.getName()));
            } else {
                ImageBox box = new ImageBox(img);
                Ui.clickable(box, new Runnable() {
                    @Override
                    public void run() {
                        showLarge(img);
                    }
                });
                card.add(box);
            }
            card.add(Ui.spacer(10));
            HBox actions = Ui.row(10);
            if (img != null) {
                actions.add(Ui.weight(Ui.soft("查看大图", new Runnable() {
                    @Override
                    public void run() {
                        showLarge(img);
                    }
                }), 1f));
            }
            actions.add(Ui.weight(Ui.outlined("删除这张", Ui.RED, new Runnable() {
                @Override
                public void run() {
                    int choice = JOptionPane.showConfirmDialog(nav, "确定删除这张课本图片吗？", "删除图片",
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (choice == JOptionPane.OK_OPTION) {
                        repo.images().delete(file);
                        nav.refresh();
                    }
                }
            }), 1f));
            card.add(actions);
            content.add(card);
        }
    }

    private void chooseImages() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("选择课本图片（可多选）");
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(new FileNameExtensionFilter("图片（JPG、PNG、GIF、BMP）", "jpg", "jpeg", "png", "gif", "bmp"));
        if (chooser.showOpenDialog(nav) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        int ok = 0;
        StringBuilder errors = new StringBuilder();
        for (File f : chooser.getSelectedFiles()) {
            try {
                repo.images().add(unit.id, f);
                ok++;
            } catch (IOException e) {
                errors.append(e.getMessage()).append('\n');
            }
        }
        nav.refresh();
        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(nav, errors.toString().trim(), "部分图片没有添加", JOptionPane.WARNING_MESSAGE);
        }
        if (ok > 0) {
            nav.toast("已添加 " + ok + " 张图片");
        }
    }

    private void showLarge(BufferedImage img) {
        java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(nav);
        JDialog dialog = new JDialog(owner, unit.displayTitle() + " · 课本图片");
        dialog.setModal(true);
        JLabel label = new JLabel(new ImageIcon(img));
        JScrollPane scroll = new JScrollPane(label);
        scroll.getVerticalScrollBar().setUnitIncrement(28);
        dialog.setContentPane(scroll);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setSize(Math.min(img.getWidth() + 40, screen.width * 9 / 10),
                Math.min(img.getHeight() + 60, screen.height * 9 / 10));
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    @Override
    public boolean handleKey(KeyEvent e) {
        if (unit == null || section != 0 || e.isAltDown() || e.isControlDown()) {
            return false;
        }
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                move(-1);
                return true;
            case KeyEvent.VK_RIGHT:
                move(1);
                return true;
            case KeyEvent.VK_SPACE:
                if (!revealed) {
                    reveal();
                }
                return true;
            case KeyEvent.VK_M:
                toggleLearned();
                return true;
            case KeyEvent.VK_S:
                Word word = currentWord();
                if (word != null) {
                    speakOrWarn(word.en);
                }
                return true;
            default:
                return false;
        }
    }
}
