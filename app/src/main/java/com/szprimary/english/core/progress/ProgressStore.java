package com.szprimary.english.core.progress;

/** 进度的持久化出口。Android 上写 SharedPreferences，测试里写内存。 */
public interface ProgressStore {
    String read();

    void write(String json);
}
