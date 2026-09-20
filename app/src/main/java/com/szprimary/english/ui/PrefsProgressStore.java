package com.szprimary.english.ui;

import android.content.Context;
import android.content.SharedPreferences;

import com.szprimary.english.core.progress.ProgressStore;

/** 用 SharedPreferences 保存学习进度，完全本地存储。 */
public final class PrefsProgressStore implements ProgressStore {
    private static final String FILE = "sz_english_progress";
    private static final String KEY = "state";

    private final SharedPreferences prefs;

    public PrefsProgressStore(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    @Override
    public String read() {
        return prefs.getString(KEY, "");
    }

    @Override
    public void write(String json) {
        prefs.edit().putString(KEY, json).apply();
    }
}
