package com.szprimary.english.desktop;

import java.io.File;
import java.util.Locale;

/** 各操作系统上保存学习进度的目录。 */
public final class AppPaths {

    static final String APP_DIR = "ShenzhenPrimaryEnglish";

    private AppPaths() {
    }

    /** 允许用 -Dszenglish.dataDir=... 指定目录（测试和便携使用）。 */
    public static File dataDir() {
        String override = System.getProperty("szenglish.dataDir");
        if (override != null && override.trim().length() > 0) {
            return new File(override.trim());
        }
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String home = System.getProperty("user.home", ".");
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            return new File(appData != null && appData.length() > 0 ? appData : home, APP_DIR);
        }
        if (os.contains("mac")) {
            return new File(home, "Library/Application Support/" + APP_DIR);
        }
        String xdg = System.getenv("XDG_DATA_HOME");
        return new File(xdg != null && xdg.length() > 0 ? xdg : home + "/.local/share", APP_DIR);
    }

    public static File progressFile() {
        return new File(dataDir(), "progress.json");
    }
}
