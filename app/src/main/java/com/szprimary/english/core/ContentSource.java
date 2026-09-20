package com.szprimary.english.core;

import java.io.IOException;

/**
 * 课程内容的读取来源。Android 上由 assets 实现，单元测试里由本地文件实现，
 * 这样核心逻辑不依赖任何 Android 类。
 */
public interface ContentSource {
    String read(String name) throws IOException;
}
