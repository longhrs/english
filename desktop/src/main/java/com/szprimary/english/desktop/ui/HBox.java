package com.szprimary.english.desktop.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * 横排容器。子组件用 client property "weight"（Float）分配剩余宽度，未设置的按首选宽度。
 * 行高取子组件中最高者；JLabel 垂直居中，其它组件撑满行高（按钮、卡片等高）。
 */
public class HBox extends JPanel implements HeightForWidth {

    public static final String WEIGHT = "weight";

    private final int gap;

    public HBox(int gap) {
        this.gap = gap;
        setOpaque(false);
        setLayout(new Layout());
    }

    private static float weight(Component c) {
        if (c instanceof JComponent) {
            Object w = ((JComponent) c).getClientProperty(WEIGHT);
            if (w instanceof Number) {
                return ((Number) w).floatValue();
            }
        }
        return 0f;
    }

    private int[] widths(int inner) {
        int n = getComponentCount();
        int[] widths = new int[n];
        int fixed = 0;
        float total = 0f;
        int visible = 0;
        for (int i = 0; i < n; i++) {
            Component c = getComponent(i);
            if (!c.isVisible()) {
                continue;
            }
            visible++;
            float w = weight(c);
            if (w > 0) {
                total += w;
            } else {
                widths[i] = c.getPreferredSize().width;
                fixed += widths[i];
            }
        }
        int remaining = Math.max(0, inner - fixed - Math.max(0, visible - 1) * gap);
        for (int i = 0; i < n; i++) {
            Component c = getComponent(i);
            float w = weight(c);
            if (c.isVisible() && w > 0) {
                widths[i] = Math.round(remaining * w / total);
            }
        }
        return widths;
    }

    @Override
    public int heightForWidth(int width) {
        Insets in = getInsets();
        int inner = Math.max(1, width - in.left - in.right);
        int[] widths = widths(inner);
        int h = 0;
        for (int i = 0; i < getComponentCount(); i++) {
            Component c = getComponent(i);
            if (c.isVisible()) {
                h = Math.max(h, HeightForWidth.heightFor(c, widths[i]));
            }
        }
        return h + in.top + in.bottom;
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        Insets in = getInsets();
        int w = in.left + in.right;
        int visible = 0;
        for (int i = 0; i < getComponentCount(); i++) {
            Component c = getComponent(i);
            if (c.isVisible()) {
                w += c.getPreferredSize().width;
                visible++;
            }
        }
        w += Math.max(0, visible - 1) * gap;
        int current = getWidth() > 0 ? getWidth() : w;
        return new Dimension(w, heightForWidth(current));
    }

    private final class Layout implements LayoutManager {
        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return getPreferredSize();
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(0, 0);
        }

        @Override
        public void layoutContainer(Container parent) {
            Insets in = getInsets();
            int inner = Math.max(1, getWidth() - in.left - in.right);
            int rowHeight = Math.max(0, getHeight() - in.top - in.bottom);
            int[] widths = widths(inner);
            int x = in.left;
            for (int i = 0; i < getComponentCount(); i++) {
                Component c = getComponent(i);
                if (!c.isVisible()) {
                    continue;
                }
                int w = widths[i];
                if (c instanceof JLabel) {
                    int h = Math.min(rowHeight, HeightForWidth.heightFor(c, w));
                    c.setBounds(x, in.top + (rowHeight - h) / 2, w, h);
                } else {
                    c.setBounds(x, in.top, w, rowHeight);
                }
                x += w + gap;
            }
        }
    }
}
