package com.szprimary.english.desktop.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

/** 按可用宽度等比缩放显示的图片（不放大超过原图）。 */
public class ImageBox extends JComponent implements HeightForWidth {

    private final BufferedImage image;
    private Image scaled;
    private int scaledWidth = -1;

    public ImageBox(BufferedImage image) {
        this.image = image;
    }

    private int displayWidth(int available) {
        return Math.max(1, Math.min(available, image.getWidth()));
    }

    @Override
    public int heightForWidth(int width) {
        int w = displayWidth(width);
        return Math.max(1, Math.round(image.getHeight() * (w / (float) image.getWidth())));
    }

    @Override
    public Dimension getPreferredSize() {
        int w = getWidth() > 0 ? getWidth() : image.getWidth();
        return new Dimension(image.getWidth(), heightForWidth(w));
    }

    @Override
    protected void paintComponent(Graphics g) {
        int w = displayWidth(getWidth());
        int h = heightForWidth(getWidth());
        if (scaled == null || scaledWidth != w) {
            scaled = w == image.getWidth() ? image : image.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            scaledWidth = w;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(scaled, (getWidth() - w) / 2, 0, w, h, null);
        g2.dispose();
    }
}
