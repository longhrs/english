package com.szprimary.english.desktop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.imageio.ImageIO;

import org.junit.Test;

public class TextbookImagesTest {

    public static File png(File dir, String name, int w, int h) throws IOException {
        File f = new File(dir, name);
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        ImageIO.write(img, "png", f);
        return f;
    }

    @Test
    public void addListLoadDelete() throws Exception {
        File tmp = Files.createTempDirectory("szimg").toFile();
        TextbookImages images = new TextbookImages(new File(tmp, "images"));
        assertTrue(images.enabled());
        assertEquals(0, images.count("g3u8"));
        File stored = images.add("g3u8", png(tmp, "page.png", 800, 600));
        images.add("g3u8", png(tmp, "page2.png", 400, 300));
        assertEquals(2, images.count("g3u8"));
        assertEquals(0, images.count("g3u9"));
        assertTrue(stored.getName().endsWith(".jpg"));
        BufferedImage loaded = images.load(stored);
        assertNotNull(loaded);
        assertEquals(800, loaded.getWidth());
        assertTrue(images.delete(stored));
        assertEquals(1, images.count("g3u8"));
    }

    @Test
    public void largePhotosAreScaledDown() throws Exception {
        File tmp = Files.createTempDirectory("szimg").toFile();
        TextbookImages images = new TextbookImages(new File(tmp, "images"));
        File stored = images.add("g4u8", png(tmp, "photo.png", 4000, 3000));
        BufferedImage img = ImageIO.read(stored);
        assertEquals(TextbookImages.MAX_WIDTH, img.getWidth());
        assertEquals(1200, img.getHeight());
    }

    @Test
    public void rejectsNonImages() throws Exception {
        File tmp = Files.createTempDirectory("szimg").toFile();
        TextbookImages images = new TextbookImages(new File(tmp, "images"));
        File txt = new File(tmp, "note.jpg");
        Files.write(txt.toPath(), "not an image".getBytes("UTF-8"));
        try {
            images.add("g3u8", txt);
            fail("应拒绝非图片文件");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("无法识别"));
        }
        assertEquals(0, images.count("g3u8"));
    }

    @Test
    public void deleteOnlyInsideImageFolder() throws Exception {
        File tmp = Files.createTempDirectory("szimg").toFile();
        TextbookImages images = new TextbookImages(new File(tmp, "images"));
        File outside = png(tmp, "keep.png", 10, 10);
        assertFalse(images.delete(outside));
        assertTrue(outside.exists());
    }

    @Test
    public void disabledStoreIsEmpty() {
        TextbookImages images = new TextbookImages(null);
        assertFalse(images.enabled());
        assertEquals(0, images.list("g3u8").size());
    }
}
