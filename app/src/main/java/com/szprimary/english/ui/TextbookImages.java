package com.szprimary.english.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 家长自己添加的课本图片（拍照或截图），按单元存在应用私有目录 files/images/单元id/ 下。
 * 用系统的「选择图片」获取，不需要任何权限；导入时把大照片缩到 1600 像素宽存为 JPEG。
 */
public final class TextbookImages {

    static final int MAX_WIDTH = 1600;

    private final File root;

    public TextbookImages(Context context) {
        this.root = new File(context.getApplicationContext().getFilesDir(), "images");
    }

    private File dir(String unitId) {
        return new File(root, unitId.replaceAll("[^A-Za-z0-9_-]", "_"));
    }

    public List<File> list(String unitId) {
        List<File> out = new ArrayList<File>();
        File[] files = dir(unitId).listFiles();
        if (files == null) {
            return out;
        }
        Arrays.sort(files);
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile() && files[i].getName().toLowerCase(Locale.ROOT).endsWith(".jpg")) {
                out.add(files[i]);
            }
        }
        return out;
    }

    public int count(String unitId) {
        return list(unitId).size();
    }

    /** 从相册导入一张图片，成功返回 true。 */
    public boolean add(ContentResolver resolver, String unitId, Uri uri) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            InputStream in = resolver.openInputStream(uri);
            if (in == null) {
                return false;
            }
            try {
                BitmapFactory.decodeStream(in, null, bounds);
            } finally {
                in.close();
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                return false;
            }
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = sampleSize(bounds.outWidth, MAX_WIDTH);
            in = resolver.openInputStream(uri);
            if (in == null) {
                return false;
            }
            Bitmap bitmap;
            try {
                bitmap = BitmapFactory.decodeStream(in, null, opts);
            } finally {
                in.close();
            }
            if (bitmap == null) {
                return false;
            }
            if (bitmap.getWidth() > MAX_WIDTH) {
                int h = Math.max(1, Math.round(bitmap.getHeight() * (MAX_WIDTH / (float) bitmap.getWidth())));
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, MAX_WIDTH, h, true);
                if (scaled != bitmap) {
                    bitmap.recycle();
                }
                bitmap = scaled;
            }
            File dir = dir(unitId);
            if (!dir.isDirectory() && !dir.mkdirs()) {
                return false;
            }
            String base = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS", Locale.ROOT).format(new Date());
            File target = new File(dir, base + ".jpg");
            for (int i = 2; target.exists(); i++) {
                target = new File(dir, base + "-" + i + ".jpg");
            }
            OutputStream out = new FileOutputStream(target);
            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            } finally {
                out.close();
                bitmap.recycle();
            }
            return true;
        } catch (IOException e) {
            return false;
        } catch (SecurityException e) {
            return false;
        } catch (OutOfMemoryError e) {
            return false;
        }
    }

    /** 按显示宽度解码，省内存。 */
    public Bitmap load(File file, int reqWidth) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), bounds);
        if (bounds.outWidth <= 0) {
            return null;
        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = sampleSize(bounds.outWidth, Math.max(200, reqWidth));
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        try {
            return BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    public boolean delete(File file) {
        try {
            if (!file.getCanonicalPath().startsWith(root.getCanonicalPath() + File.separator)) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
        return file.delete();
    }

    private static int sampleSize(int width, int target) {
        int sample = 1;
        while (width / (sample * 2) >= target) {
            sample *= 2;
        }
        return sample;
    }
}
