package com.szprimary.english.desktop.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.border.EmptyBorder;

/** 圆角卡片：竖排内容 + 背景色 + 可选描边。 */
public class Card extends VBox {

    private Color fill;
    private Color stroke;
    private final int radius;
    private boolean hover;

    public Card(Color fill, Color stroke, int radius, int padding, int gap) {
        super(gap);
        this.fill = fill;
        this.stroke = stroke;
        this.radius = radius;
        setBorder(new EmptyBorder(padding, padding + 2, padding, padding + 2));
    }

    public void setFill(Color fill) {
        this.fill = fill;
        repaint();
    }

    void setHover(boolean hover) {
        this.hover = hover;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RoundRectangle2D shape = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f,
                radius * 2f, radius * 2f);
        if (fill != null) {
            g2.setColor(hover ? Ui.mix(fill, Ui.PRIMARY, 0.06f) : fill);
            g2.fill(shape);
        }
        Color border = hover ? Ui.mix(Ui.LINE, Ui.PRIMARY, 0.5f) : stroke;
        if (border != null) {
            g2.setColor(border);
            g2.draw(shape);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
