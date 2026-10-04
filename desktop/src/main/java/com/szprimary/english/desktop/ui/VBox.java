package com.szprimary.english.desktop.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JPanel;

/**
 * 竖排容器：子组件默认撑满宽度，高度按宽度计算。
 * 子组件可用 client property "align" = "left" / "center" 改为按首选宽度放置。
 */
public class VBox extends JPanel implements HeightForWidth {

    public static final String ALIGN = "align";

    private final int gap;

    public VBox(int gap) {
        this.gap = gap;
        setOpaque(false);
        setLayout(new Layout());
    }

    @Override
    public int heightForWidth(int width) {
        Insets in = getInsets();
        int inner = Math.max(1, width - in.left - in.right);
        int h = in.top + in.bottom;
        int count = 0;
        for (int i = 0; i < getComponentCount(); i++) {
            Component c = getComponent(i);
            if (!c.isVisible()) {
                continue;
            }
            h += HeightForWidth.heightFor(c, childWidth(c, inner));
            count++;
        }
        return h + Math.max(0, count - 1) * gap;
    }

    private int naturalWidth() {
        Insets in = getInsets();
        int w = 0;
        for (int i = 0; i < getComponentCount(); i++) {
            Component c = getComponent(i);
            if (c.isVisible()) {
                w = Math.max(w, HeightForWidth.naturalWidth(c));
            }
        }
        return w + in.left + in.right;
    }

    private static int childWidth(Component c, int inner) {
        Object align = c instanceof javax.swing.JComponent ? ((javax.swing.JComponent) c).getClientProperty(ALIGN) : null;
        if (align == null) {
            return inner;
        }
        return Math.min(inner, c.getPreferredSize().width);
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        int natural = naturalWidth();
        int w = getWidth() > 0 ? getWidth() : natural;
        return new Dimension(natural, heightForWidth(w));
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
            int y = in.top;
            for (int i = 0; i < getComponentCount(); i++) {
                Component c = getComponent(i);
                if (!c.isVisible()) {
                    continue;
                }
                int w = childWidth(c, inner);
                int h = HeightForWidth.heightFor(c, w);
                int x = in.left;
                Object align = c instanceof javax.swing.JComponent
                        ? ((javax.swing.JComponent) c).getClientProperty(ALIGN) : null;
                if ("center".equals(align)) {
                    x = in.left + (inner - w) / 2;
                }
                c.setBounds(x, y, w, h);
                y += h + gap;
            }
        }
    }
}
