package com.szprimary.english.desktop.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;

/** 细长圆角进度条。 */
public class ProgressBar extends JComponent {

    private final int percent;

    public ProgressBar(int percent) {
        this.percent = Math.max(0, Math.min(100, percent));
        setPreferredSize(new Dimension(100, 18));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int h = 8;
        int y = (getHeight() - h) / 2;
        g2.setColor(Ui.LINE);
        g2.fill(new RoundRectangle2D.Float(0, y, getWidth(), h, h, h));
        if (percent > 0) {
            g2.setColor(Ui.PRIMARY);
            g2.fill(new RoundRectangle2D.Float(0, y, Math.max(h, getWidth() * percent / 100f), h, h, h));
        }
        g2.dispose();
    }
}
