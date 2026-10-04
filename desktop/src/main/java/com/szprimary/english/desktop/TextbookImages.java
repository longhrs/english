package com.szprimary.english.desktop;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * 家长自己添加的课本图片（拍照或截图），按单元保存在本机数据目录的 images/单元id/ 下。
 * 图片不随程序打包、不上传；导入时把过大的照片缩到 1600 像素宽并存为 JPEG，避免占用太多内存。
 */
public final class TextbookImages {

    static final int MAX_WIDTH = 1600;

    private final File root;
    private final Map<String, BufferedImage> cache = new HashMap<String, BufferedImage>();

    /** root 为 null 时功能关闭（例如单元测试里的内存版数据）。 */
    public TextbookImages(File root) {
        this.root = root;
    }

    public boolean enabled() {
        return root != null;
    }

    public File root() {
        return root;
    }

    private File dir(String unitId) {
        return new File(root, unitId.replaceAll("[^A-Za-z0-9_-]", "_"));
    }

    public List<File> list(String unitId) {
        List<File> out = new ArrayList<File>();
        if (root == null) {
            return out;
        }
        File[] files = dir(unitId).listFiles();
        if (files == null) {
            return out;
        }
        Arrays.sort(files);
        for (File f : files) {
            String name = f.getName().toLowerCase(Locale.ROOT);
            if (f.isFile() && (name.endsWith(".jpg") || name.endsWith(".png"))) {
                out.add(f);
            }
        }
        return out;
    }

    public int count(String unitId) {
        return list(unitId).size();
    }

    /** 导入一张图片；不是 Java 能读的图片格式（如 HEIC、WebP）时抛出 IOException。 */
    public File add(String unitId, File source) throws IOException {
        if (root == null) {
            throw new IOException("图片功能未开启");
        }
        BufferedImage img = ImageIO.read(source);
        if (img == null) {
            throw new IOException("无法识别的图片格式：" + source.getName() + "（支持 JPG、PNG、GIF、BMP）");
        }
        int w = img.getWidth();
        int h = img.getHeight();
        if (w > MAX_WIDTH) {
            h = Math.max(1, Math.round(h * (MAX_WIDTH / (float) w)));
            w = MAX_WIDTH;
        }
        BufferedImage rgb = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.drawImage(img, 0, 0, w, h, null);
        g.dispose();
        File dir = dir(unitId);
        if (!dir.isDirectory() && !dir.mkdirs()) {
            throw new IOException("无法创建目录：" + dir);
        }
        String base = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS", Locale.ROOT).format(new Date());
        File target = new File(dir, base + ".jpg");
        for (int i = 2; target.exists(); i++) {
            target = new File(dir, base + "-" + i + ".jpg");
        }
        if (!ImageIO.write(rgb, "jpg", target)) {
            throw new IOException("保存图片失败");
        }
        return target;
    }

    /** 只删除本功能目录里的文件。 */
    public boolean delete(File file) {
        if (root == null || file == null) {
            return false;
        }
        try {
            String rootPath = root.getCanonicalPath() + File.separator;
            if (!file.getCanonicalPath().startsWith(rootPath)) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
        cache.remove(file.getAbsolutePath());
        return file.delete();
    }

    /** 读取图片（带缓存，页面刷新时不重复解码）。 */
    public BufferedImage load(File file) {
        String key = file.getAbsolutePath() + "@" + file.lastModified();
        BufferedImage img = cache.get(key);
        if (img == null) {
            try {
                img = ImageIO.read(file);
            } catch (IOException e) {
                img = null;
            }
            if (img != null) {
                cache.put(key, img);
            }
        }
        return img;
    }
}
