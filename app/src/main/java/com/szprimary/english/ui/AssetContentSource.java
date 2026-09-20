package com.szprimary.english.ui;

import android.content.Context;

import com.szprimary.english.core.ContentSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/** 从 APK 的 assets 目录读取课程内容。 */
public final class AssetContentSource implements ContentSource {
    private final Context context;

    public AssetContentSource(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public String read(String name) throws IOException {
        InputStream in = context.getAssets().open(name);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                sb.append(line).append('\n');
                line = reader.readLine();
            }
            return sb.toString();
        } finally {
            in.close();
        }
    }
}
