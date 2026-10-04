package com.szprimary.english.desktop.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JPanel;

/** 自动折行的横排容器，用于连词成句的词块。 */
public class WrapBox extends JPanel implements HeightForWidth {

    private final int hgap;
    private final int vgap;

    public WrapBox(int hgap, int vgap) {
        this.hgap = hgap;
        this.vgap = vgap;
        setOpaque(false);
        setLayout(new Layout());
    }

    /** 按给定宽度排布；place 为 true 时同时设置子组件位置。返回总高度。 */
    private int flow(int width, boolean place) {
        Insets in = getInsets();
        int inner = Math.max(1, width - in.left - in.right);
        int x = 0;
        int y = in.top;
        int lineHeight = 0;
        boolean any = false;
        for (int i = 0; i < getComponentCount(); i++) {
            Component c = getComponent(i);
            if (!c.isVisible()) {
                continue;
            }
            Dimension d = c.getPreferredSize();
            int w = Math.min(d.width, inner);
            if (x > 0 && x + w > inner) {
                y += lineHeight + vgap;
                x = 0;
                lineHeight = 0;
            }
            if (place) {
                c.setBounds(in.left + x, y, w, d.height);
            }
            x += w + hgap;
            lineHeight = Math.max(lineHeight, d.height);
            any = true;
        }
        return (any ? y + lineHeight : in.top) + in.bottom;
    }

    @Override
    public int heightForWidth(int width) {
        return flow(width, false);
    }

    @Override
    public Dimension getPreferredSize() {
        int w = getWidth() > 0 ? getWidth() : 400;
        return new Dimension(w, heightForWidth(w));
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
            flow(getWidth(), true);
        }
    }
}
