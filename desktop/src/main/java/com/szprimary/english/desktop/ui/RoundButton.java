package com.szprimary.english.desktop.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

/** 圆角按钮，配色与 Android 版一致。 */
public class RoundButton extends JButton {

    private Color fill;
    private Color stroke;
    private final int radius;

    public RoundButton(String label, Color fill, Color text, Color stroke) {
        super(label);
        this.fill = fill;
        this.stroke = stroke;
        this.radius = 10;
        setForeground(text);
        setFont(Ui.font(15, true));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setBorder(new EmptyBorder(10, 18, 10, 18));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void setColors(Color fill, Color text, Color stroke) {
        this.fill = fill;
        this.stroke = stroke;
        setForeground(text);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ButtonModel m = getModel();
        RoundRectangle2D shape = new RoundRectangle2D.Float(1f, 1f, getWidth() - 2f, getHeight() - 2f,
                radius * 2f, radius * 2f);
        if (fill != null && fill.getAlpha() > 0) {
            Color c = fill;
            if (!isEnabled()) {
                c = Ui.mix(fill, Color.WHITE, 0.45f);
            } else if (m.isPressed()) {
                c = Ui.mix(fill, Color.BLACK, 0.15f);
            } else if (m.isRollover()) {
                c = Ui.mix(fill, Color.BLACK, 0.07f);
            }
            g2.setColor(c);
            g2.fill(shape);
        } else if (m.isRollover() && isEnabled()) {
            g2.setColor(Ui.SOFT);
            g2.fill(shape);
        }
        if (stroke != null) {
            g2.setColor(m.isRollover() && isEnabled() ? Ui.mix(stroke, Ui.PRIMARY, 0.5f) : stroke);
            g2.draw(shape);
        }
        if (isFocusOwner()) {
            g2.setColor(Ui.mix(Ui.PRIMARY, Color.WHITE, 0.35f));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new RoundRectangle2D.Float(2f, 2f, getWidth() - 4f, getHeight() - 4f,
                    radius * 2f, radius * 2f));
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
