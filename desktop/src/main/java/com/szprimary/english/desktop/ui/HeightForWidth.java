package com.szprimary.english.desktop.ui;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JTextArea;

/** 高度随宽度变化的组件（自动换行的文字、包含它们的容器）。Swing 自带布局不处理这种情况。 */
public interface HeightForWidth {

    int heightForWidth(int width);

    static int heightFor(Component c, int width) {
        if (!c.isVisible()) {
            return 0;
        }
        if (c instanceof HeightForWidth) {
            return ((HeightForWidth) c).heightForWidth(width);
        }
        if (c instanceof JTextArea) {
            // 自动换行的 JTextArea 按当前宽度计算首选高度，先给它设定宽度
            c.setSize(Math.max(1, width), Short.MAX_VALUE);
            return c.getPreferredSize().height;
        }
        return c.getPreferredSize().height;
    }

    static int naturalWidth(Component c) {
        Dimension d = c.getPreferredSize();
        return d.width;
    }
}
