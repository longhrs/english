package com.szprimary.english.desktop;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/** 程序图标：与 Android 启动图标相同，品牌蓝底上一个白色字母 A。 */
public final class IconFactory {

    private IconFactory() {
    }

    public static BufferedImage render(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        float arc = size * 0.42f;
        g.setColor(new Color(0x1B6FE3));
        g.fill(new RoundRectangle2D.Float(0, 0, size, size, arc, arc));
        // 坐标沿用 Android 矢量图的 108×108 画布
        double s = size / 108.0;
        Path2D.Double a = new Path2D.Double();
        a.moveTo(47 * s, 26 * s);
        a.lineTo(61 * s, 26 * s);
        a.lineTo(79 * s, 84 * s);
        a.lineTo(66 * s, 84 * s);
        a.lineTo(54 * s, 44 * s);
        a.lineTo(42 * s, 84 * s);
        a.lineTo(29 * s, 84 * s);
        a.closePath();
        g.setColor(Color.WHITE);
        g.fill(a);
        g.fill(new java.awt.geom.Rectangle2D.Double(41 * s, 60 * s, 26 * s, 9 * s));
        g.dispose();
        return img;
    }

    public static List<Image> windowIcons() {
        List<Image> icons = new ArrayList<Image>();
        int[] sizes = {16, 24, 32, 48, 64, 128, 256};
        for (int i = 0; i < sizes.length; i++) {
            icons.add(render(sizes[i]));
        }
        return icons;
    }
}
