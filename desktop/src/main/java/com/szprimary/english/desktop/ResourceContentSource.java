package com.szprimary.english.desktop;

import com.szprimary.english.core.ContentSource;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** 从 classpath 读取课程数据（打包时来自 app/src/main/assets，与 Android 版同一份文件）。 */
public final class ResourceContentSource implements ContentSource {

    @Override
    public String read(String name) throws IOException {
        InputStream in = ResourceContentSource.class.getResourceAsStream("/" + name);
        if (in == null) {
            throw new FileNotFoundException("缺少课程文件：" + name);
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } finally {
            in.close();
        }
    }
}
