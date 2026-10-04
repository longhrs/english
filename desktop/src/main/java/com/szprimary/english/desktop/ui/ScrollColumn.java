package com.szprimary.english.desktop.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;

/** 页面滚动区：宽度跟随窗口，内容居中且限制最大宽度，宽屏上也便于阅读。 */
public class ScrollColumn extends JPanel implements Scrollable {

    private static final int MAX_WIDTH = 880;
    private static final int PAD_X = 24;
    private static final int PAD_TOP = 18;
    private static final int PAD_BOTTOM = 36;

    private final Component content;

    public ScrollColumn(Component content) {
        this.content = content;
        setBackground(Ui.BG);
        setOpaque(true);
        setLayout(new Layout());
        add(content);
    }

    private int availableWidth() {
        Container p = getParent();
        if (p instanceof JViewport && p.getWidth() > 0) {
            return p.getWidth();
        }
        return getWidth() > 0 ? getWidth() : MAX_WIDTH + PAD_X * 2;
    }

    private int contentWidth(int total) {
        return Math.max(200, Math.min(MAX_WIDTH, total - PAD_X * 2));
    }

    @Override
    public Dimension getPreferredSize() {
        int w = availableWidth();
        int h = PAD_TOP + HeightForWidth.heightFor(content, contentWidth(w)) + PAD_BOTTOM;
        return new Dimension(w, h);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 28;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return Math.max(28, visibleRect.height - 60);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        Container p = getParent();
        return p instanceof JViewport && p.getHeight() > getPreferredSize().height;
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
            int cw = contentWidth(getWidth());
            int h = HeightForWidth.heightFor(content, cw);
            content.setBounds((getWidth() - cw) / 2, PAD_TOP, cw, h);
        }
    }
}
