package com.szprimary.english.desktop.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.text.StyleContext;

/** 配色、字体和常用组件，命名与 Android 版的 Ui 对应。 */
public final class Ui {

    public static final Color PRIMARY = new Color(0x1B6FE3);
    public static final Color PRIMARY_DARK = new Color(0x14539F);
    public static final Color ACCENT = new Color(0xFF8A3D);
    public static final Color BG = new Color(0xF4F6FB);
    public static final Color CARD = new Color(0xFFFFFF);
    public static final Color TEXT = new Color(0x1F2430);
    public static final Color SUB = new Color(0x6B7280);
    public static final Color LINE = new Color(0xE3E7EF);
    public static final Color SOFT = new Color(0xE8EEFB);
    public static final Color GREEN = new Color(0x1EA672);
    public static final Color RED = new Color(0xE5484D);
    public static final Color GOLD = new Color(0xF5A623);
    public static final Color GREEN_BG = new Color(0xE9F7F1);
    public static final Color RED_BG = new Color(0xFDECEC);
    public static final Color WHITE = Color.WHITE;

    /** 依次尝试的中文界面字体；都没有时用 Java 逻辑字体 Dialog。 */
    private static final String[] FAMILIES = {
            "Microsoft YaHei UI", "Microsoft YaHei", "PingFang SC", "Hiragino Sans GB",
            "Noto Sans CJK SC", "Noto Sans SC", "Source Han Sans SC", "WenQuanYi Zen Hei", "Dialog"
    };

    private static String family;

    private Ui() {
    }

    static synchronized String family() {
        if (family == null) {
            Set<String> installed = new HashSet<String>(Arrays.asList(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
            family = "Dialog";
            for (int i = 0; i < FAMILIES.length; i++) {
                if (installed.contains(FAMILIES[i])) {
                    family = FAMILIES[i];
                    break;
                }
            }
        }
        return family;
    }

    /**
     * 通过 StyleContext 取字体：返回的字体带系统字体回退，
     * 主字体缺少音标符号（ˈ ə ɪ θ 等）时会自动借用其它字体显示，不会出现方框。
     */
    public static Font font(float size, boolean bold) {
        return StyleContext.getDefaultStyleContext().getFont(family(), bold ? Font.BOLD : Font.PLAIN, Math.round(size));
    }

    public static Color mix(Color a, Color b, float t) {
        float s = 1f - t;
        return new Color(Math.round(a.getRed() * s + b.getRed() * t),
                Math.round(a.getGreen() * s + b.getGreen() * t),
                Math.round(a.getBlue() * s + b.getBlue() * t));
    }

    // ---- 文字 ----

    public static Text text(String value, float size, Color color, boolean bold) {
        return new Text(value, font(size, bold), color);
    }

    public static Text body(String value) {
        return text(value, 15, TEXT, false);
    }

    public static Text hint(String value) {
        return text(value, 13, SUB, false);
    }

    /** 不换行的短文字，可居中（大号分数、星星等）。 */
    public static JLabel label(String value, float size, Color color, boolean bold) {
        JLabel label = new JLabel(value);
        label.setFont(font(size, bold));
        label.setForeground(color);
        return label;
    }

    public static JLabel centered(String value, float size, Color color, boolean bold) {
        JLabel label = label(value, size, color, bold);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    // ---- 容器 ----

    public static VBox column(int gap) {
        return new VBox(gap);
    }

    public static HBox row(int gap) {
        return new HBox(gap);
    }

    public static Card card() {
        return new Card(CARD, LINE, 14, 16, 0);
    }

    public static Card card(Color fill) {
        return new Card(fill, null, 14, 16, 0);
    }

    public static <T extends JComponent> T weight(T component, float weight) {
        component.putClientProperty(HBox.WEIGHT, weight);
        return component;
    }

    public static <T extends JComponent> T align(T component, String align) {
        component.putClientProperty(VBox.ALIGN, align);
        return component;
    }

    public static JComponent spacer(int height) {
        JComponent c = new JComponent() {
        };
        c.setPreferredSize(new Dimension(1, height));
        return c;
    }

    public static JComponent divider() {
        JComponent c = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(LINE);
                g.fillRect(0, getHeight() / 2, getWidth(), 1);
            }
        };
        c.setPreferredSize(new Dimension(1, 21));
        return c;
    }

    public static ProgressBar progress(int percent) {
        return new ProgressBar(percent);
    }

    public static String stars(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append(i < count ? "★" : "☆");
        }
        return sb.toString();
    }

    // ---- 按钮 ----

    public static RoundButton button(String label, Color fill, Color text, Runnable action) {
        RoundButton b = new RoundButton(label, fill, text, null);
        onClick(b, action);
        return b;
    }

    public static RoundButton primary(String label, Runnable action) {
        return button(label, PRIMARY, WHITE, action);
    }

    public static RoundButton soft(String label, Runnable action) {
        return button(label, SOFT, PRIMARY, action);
    }

    public static RoundButton outlined(String label, Color text, Runnable action) {
        RoundButton b = new RoundButton(label, CARD, text, LINE);
        onClick(b, action);
        return b;
    }

    private static void onClick(RoundButton b, final Runnable action) {
        if (action != null) {
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    action.run();
                }
            });
        }
    }

    /** 让整块区域（含其中的文字）可点击，带悬停效果。 */
    public static void clickable(final JComponent target, final Runnable action) {
        final MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    action.run();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (target instanceof Card) {
                    ((Card) target).setHover(true);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (target instanceof Card && target.getMousePosition(true) == null) {
                    ((Card) target).setHover(false);
                }
            }
        };
        install(target, adapter);
    }

    private static void install(Component c, MouseAdapter adapter) {
        c.addMouseListener(adapter);
        c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (c instanceof Container) {
            Component[] children = ((Container) c).getComponents();
            for (int i = 0; i < children.length; i++) {
                install(children[i], adapter);
            }
        }
    }
}
