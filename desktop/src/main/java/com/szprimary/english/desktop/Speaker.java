package com.szprimary.english.desktop;

import java.io.File;
import java.util.Locale;

/**
 * 朗读器。PC 上没有统一的 TTS 接口，按操作系统调用自带的语音引擎：
 * Windows 用 System.Speech（SAPI），macOS 用 say，Linux 用 espeak-ng / espeak / spd-say。
 */
public interface Speaker {

    /** 是否找到了可用的语音引擎。 */
    boolean isAvailable();

    /** 给用户看的一句话状态说明（显示在「关于与设置」）。 */
    String status();

    /** 朗读一段英文，会打断正在读的内容。 */
    void speak(String text);

    void shutdown();

    /** 按当前操作系统选择实现；找不到引擎时返回一个静默实现，界面照常可用。 */
    static Speaker create() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return new WindowsSpeaker();
        }
        if (os.contains("mac")) {
            File say = findExecutable("say");
            return say == null ? new Silent("未找到 macOS 的 say 命令，朗读不可用。") : ProcessSpeaker.mac(say);
        }
        File espeakNg = findExecutable("espeak-ng");
        if (espeakNg != null) {
            return ProcessSpeaker.espeak(espeakNg);
        }
        File espeak = findExecutable("espeak");
        if (espeak != null) {
            return ProcessSpeaker.espeak(espeak);
        }
        File spd = findExecutable("spd-say");
        if (spd != null) {
            return ProcessSpeaker.spdSay(spd);
        }
        return new Silent("未找到语音引擎。安装 espeak-ng（如 sudo apt install espeak-ng）后重启即可朗读。");
    }

    /**
     * 只保留可打印的 ASCII 字符再交给外部进程：课程里要读的都是英文，
     * 这样既不会遇到控制台编码问题，也不会有换行把一句话拆成两次朗读。
     */
    static String clean(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(Math.min(text.length(), 300));
        boolean space = false;
        for (int i = 0; i < text.length() && sb.length() < 300; i++) {
            char c = text.charAt(i);
            if (c == '’' || c == '‘') {
                c = '\'';
            }
            if (c >= 0x21 && c <= 0x7E) {
                sb.append(c);
                space = false;
            } else if (!space && sb.length() > 0) {
                sb.append(' ');
                space = true;
            }
        }
        String out = sb.toString().trim();
        // 不以 - 开头，免得被命令行工具当成参数
        while (out.startsWith("-")) {
            out = out.substring(1).trim();
        }
        return out;
    }

    static File findExecutable(String name) {
        String path = System.getenv("PATH");
        if (path == null) {
            return null;
        }
        String[] dirs = path.split(File.pathSeparator);
        for (int i = 0; i < dirs.length; i++) {
            File f = new File(dirs[i], name);
            if (f.isFile() && f.canExecute()) {
                return f;
            }
        }
        return null;
    }

    /** 没有语音引擎时的占位实现。 */
    final class Silent implements Speaker {
        private final String status;

        public Silent(String status) {
            this.status = status;
        }

        @Override
        public boolean isAvailable() {
            return false;
        }

        @Override
        public String status() {
            return status;
        }

        @Override
        public void speak(String text) {
        }

        @Override
        public void shutdown() {
        }
    }
}
