package com.szprimary.english.desktop.ui;

import java.awt.event.KeyEvent;

/** 一个页面，对应 Android 版的一个 Activity。每次显示或刷新都重新 build。 */
public interface Page {

    String title();

    /** 标题栏右侧的小字，可为 null。在 build 之后读取，可以反映最新状态。 */
    String subtitle();

    void build(VBox content);

    /** 页面级快捷键；处理了返回 true。 */
    default boolean handleKey(KeyEvent e) {
        return false;
    }
}
